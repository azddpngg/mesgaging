package com.example.mesgaging.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesgaging.model.EmojiBurstParticle
import kotlin.math.roundToInt

@Composable
fun EmojiBurstLayer(
    particles: List<EmojiBurstParticle>,
    onParticleFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            SingleEmojiParticle(
                particle = particle,
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx,
                onFinished = { onParticleFinished(particle.id) }
            )
        }
    }
}

@Composable
private fun SingleEmojiParticle(
    particle: EmojiBurstParticle,
    screenWidthPx: Float,
    screenHeightPx: Float,
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(particle.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
        )
        onFinished()
    }

    val t = progress.value
    // Ease out trajectory
    val currentX = (particle.startXRatio + (particle.targetXRatio - particle.startXRatio) * t) * screenWidthPx
    // Rise upwards with slight wobble
    val riseDistance = 350f * t
    val currentY = (particle.startYRatio * screenHeightPx) - riseDistance
    val currentAlpha = (1f - (t * t)).coerceIn(0f, 1f)
    val currentScale = (particle.scale * (0.8f + (1f - t) * 0.5f)).coerceAtLeast(0.1f)
    val currentRotation = particle.rotation + (t * 180f)

    Box(
        modifier = Modifier
            .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
            .scale(currentScale)
            .rotate(currentRotation)
            .alpha(currentAlpha)
    ) {
        Text(
            text = particle.emoji,
            fontSize = 32.sp
        )
    }
}
