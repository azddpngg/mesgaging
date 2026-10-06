package com.example.mesgaging.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.random.Random

/**
 * Aerodynamic flight configuration randomized on each app startup.
 */
private data class FlightTrajectory(
    val patternType: Int,     // 0: Sweeping Wingover, 1: High S-Curve, 2: Slingshot Arc, 3: Corkscrew Climb
    val exitDirection: Float, // 1f (rightward) or -1f (leftward)
    val span: Float,          // Maximum horizontal reach in dp
    val totalRollSpins: Float // Total 360°/720° spins in degrees
)

/**
 * Smooth parametric position calculation evaluated analytically per-frame.
 * Guarantees 100% mathematical continuity, zero frame-rate stutter, and sub-pixel alignment.
 */
private fun getTrajectoryPoint(p: Float, trajectory: FlightTrajectory): Pair<Float, Float> {
    val totalClimb = -920f

    val (x, y) = when (trajectory.patternType) {
        0 -> {
            // Sweeping Wingover: Graceful parabolic arc banking wide before rocketing skyward
            val x = sin(p * PI.toFloat()) * trajectory.span * trajectory.exitDirection
            val y = p * totalClimb - (sin(p * PI.toFloat() * 0.5f) * 35f)
            Pair(x, y)
        }
        1 -> {
            // High S-Curve: Elegant double-curved slalom
            val x = sin(p * PI.toFloat() * 1.6f) * (trajectory.span * 0.85f) * trajectory.exitDirection
            val y = p * totalClimb
            Pair(x, y)
        }
        2 -> {
            // Slingshot Arc: Subtle gathering dip followed by exponential acceleration
            val dip = if (p < 0.2f) (sin(p * 5f * PI.toFloat()) * 20f) else 0f
            val x = sin(p * PI.toFloat() * 1.25f) * trajectory.span * trajectory.exitDirection
            val y = (p * totalClimb) + dip
            Pair(x, y)
        }
        else -> {
            // Corkscrew Climb: Tight aerodynamic spiral expanding upwards
            val angle = p * PI.toFloat() * 2.2f
            val radius = sin(p * PI.toFloat()) * 42f
            val x = (sin(angle) * radius + p * trajectory.span * 0.5f) * trajectory.exitDirection
            val y = p * totalClimb
            Pair(x, y)
        }
    }
    return Pair(x, y)
}

/**
 * Calculates instantaneous aerodynamic bank angle (in degrees) from the trajectory tangent vector.
 */
private fun getAerodynamicBankAngle(p: Float, trajectory: FlightTrajectory): Float {
    val dt = 0.005f
    val p1 = (p - dt).coerceAtLeast(0f)
    val p2 = (p + dt).coerceAtMost(1f)
    val (x1, y1) = getTrajectoryPoint(p1, trajectory)
    val (x2, y2) = getTrajectoryPoint(p2, trajectory)
    val dx = x2 - x1
    val dy = y2 - y1
    // Tangent heading angle: 0° is straight up (-Y), positive is banking right, negative is banking left
    val angleRad = atan2(dx, -dy)
    return (angleRad * 180f / PI.toFloat()).coerceIn(-65f, 65f)
}

/**
 * High-craft, bespoke startup animation:
 * - Butter-smooth continuous 60/120Hz analytical curve rendering (no discrete coroutine sampling lag).
 * - Authentic 3D aerodynamic roll & tangent banking instead of a flat 2D sticker spin.
 * - Minimalist luxury styling: razor-sharp pure white luminous line against OLED pitch black.
 * - Gentle typography fade and silky UI reveal.
 */
@Composable
fun StartupBirdOverlay(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSkipped by remember { mutableStateOf(false) }

    // Synchronized Animatable values driven by Compose's display vsync clock
    val progress = remember { Animatable(0f) }
    val birdScale = remember { Animatable(0.92f) }
    val birdAlpha = remember { Animatable(1f) }
    val rollProgress = remember { Animatable(0f) }
    val flapScale = remember { Animatable(1f) }
    val lineAlpha = remember { Animatable(1f) }
    val textAlpha = remember { Animatable(0.75f) }
    val splashAlpha = remember { Animatable(1f) }

    // Randomized flight path configuration generated uniquely on every startup
    val trajectory = remember {
        val pattern = Random.nextInt(4)
        val exitDir = if (Random.nextBoolean()) 1f else -1f
        val span = Random.nextInt(130, 210).toFloat()
        val spinCount = if (Random.nextBoolean()) 1f else 2f
        val spinDirection = if (Random.nextBoolean()) 1f else -1f
        FlightTrajectory(
            patternType = pattern,
            exitDirection = exitDir,
            span = span,
            totalRollSpins = spinCount * 360f * spinDirection
        )
    }

    fun skip() {
        if (!isSkipped) {
            isSkipped = true
            onAnimationComplete()
        }
    }

    LaunchedEffect(Unit) {
        // Phase 1: Subtle initial poise / breath (0 - 180ms)
        birdScale.animateTo(1.0f, tween(180, easing = FastOutSlowInEasing))

        // Phase 2: Flight Launch & Aerodynamic Ascent (180ms - 1050ms)
        coroutineScope {
            // Text subtitle dissolves as flight begins
            launch {
                textAlpha.animateTo(0f, tween(160, easing = LinearEasing))
            }

            // Wing flex oscillation during acceleration
            launch {
                repeat(4) {
                    flapScale.animateTo(0.78f, tween(65, easing = FastOutLinearInEasing))
                    flapScale.animateTo(1.06f, tween(75, easing = FastOutSlowInEasing))
                }
                flapScale.animateTo(1.0f, tween(60))
            }

            // Primary trajectory progress (0f -> 1f) with buttery smooth easing
            launch {
                progress.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(
                        durationMillis = 880,
                        easing = CubicBezierEasing(0.22f, 0.08f, 0.20f, 1.0f)
                    )
                )
            }

            // Authentic 3D Axial Roll (Barrel Roll / Aerial Spin)
            launch {
                delay(120)
                rollProgress.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(
                        durationMillis = 740,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            // Natural altitude perspective scale (gently scales down as it climbs away)
            launch {
                delay(220)
                birdScale.animateTo(0.35f, tween(660, easing = FastOutLinearInEasing))
            }

            // Bird disappears into the distance
            launch {
                delay(680)
                birdAlpha.animateTo(0f, tween(200, easing = FastOutLinearInEasing))
            }

            // Luminous line dissolves cleanly as bird exits
            launch {
                delay(760)
                lineAlpha.animateTo(0f, tween(240, easing = FastOutSlowInEasing))
            }
        }

        // Phase 3: Seamless silky dissolve to the messaging UI
        splashAlpha.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
        onAnimationComplete()
    }

    if (!isSkipped && splashAlpha.value > 0.01f) {
        val currentP = progress.value
        val (curX, curY) = getTrajectoryPoint(currentP, trajectory)
        val dynamicBank = getAerodynamicBankAngle(currentP, trajectory)
        val currentRollY = rollProgress.value * trajectory.totalRollSpins

        Box(
            modifier = modifier
                .fillMaxSize()
                .alpha(splashAlpha.value)
                .background(Color(0xFF000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { skip() }
                )
                .testTag("startup_bird_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // 1. CANVASES: Butter-Smooth Analytical Line Trail
            // Rendered per-frame along the exact parametric trajectory equation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                if (currentP > 0.005f) {
                    val segments = (currentP * 90).toInt().coerceIn(4, 90)
                    val trailPath = Path()

                    val (startX, startY) = getTrajectoryPoint(0f, trajectory)
                    trailPath.moveTo(cx + startX.dp.toPx(), cy + startY.dp.toPx())

                    for (i in 1..segments) {
                        val t = (i.toFloat() / 90f).coerceAtMost(currentP)
                        val (segX, segY) = getTrajectoryPoint(t, trajectory)
                        trailPath.lineTo(cx + segX.dp.toPx(), cy + segY.dp.toPx())
                    }

                    // Ensure exact tip aligns with current bird center
                    trailPath.lineTo(cx + curX.dp.toPx(), cy + curY.dp.toPx())

                    val effectiveAlpha = (lineAlpha.value * splashAlpha.value).coerceIn(0f, 1f)

                    // Subtle soft ambient glow aura (clean, restrained, non-cluttered)
                    drawPath(
                        path = trailPath,
                        color = Color.White.copy(alpha = 0.16f * effectiveAlpha),
                        style = Stroke(
                            width = 6.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Crisp razor-sharp luminous core line
                    drawPath(
                        path = trailPath,
                        color = Color.White.copy(alpha = 0.92f * effectiveAlpha),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Gentle leading apex point
                    drawCircle(
                        color = Color.White.copy(alpha = effectiveAlpha),
                        radius = 2.5.dp.toPx(),
                        center = Offset(cx + curX.dp.toPx(), cy + curY.dp.toPx())
                    )
                }
            }

            // 2. THE SOARING BIRD: Authentic 3D aerodynamics & perspective roll
            Box(
                modifier = Modifier
                    .offset(x = curX.dp, y = curY.dp)
                    .graphicsLayer {
                        // Dynamic aerodynamic bank angle following the tangent of flight
                        rotationZ = dynamicBank
                        // Authentic 3D axial barrel roll around longitudinal axis
                        rotationY = currentRollY
                        cameraDistance = 16f * density

                        val sx = (birdScale.value * flapScale.value).coerceAtLeast(0.05f)
                        val sy = birdScale.value.coerceAtLeast(0.05f)
                        scaleX = sx
                        scaleY = sy
                        alpha = birdAlpha.value
                    }
                    .size(62.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bird_logo),
                    contentDescription = "Bird Logo",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            // 3. MINIMALIST LUXURY SUBTITLE: Refined typographic settle at launch
            if (textAlpha.value > 0.01f) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 52.dp)
                        .alpha(textAlpha.value * splashAlpha.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "mesgaging",
                        color = Color(0x99FFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )
                }
            }
        }
    }
}
