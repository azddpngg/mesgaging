package com.example.mesgaging.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StopScreenShare
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesgaging.model.ScreenShareSession
import kotlinx.coroutines.delay

@Composable
fun ScreenShareOverlay(
    session: ScreenShareSession,
    onStopScreenShare: () -> Unit,
    onToggleAudio: () -> Unit,
    onTogglePause: () -> Unit,
    onTogglePrivacyShield: () -> Unit,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(session.id) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_badge"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false) {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF18181B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // LIVE Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDC2626).copy(alpha = alphaAnim)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Screen Sharing to ${session.targetUserName}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${session.resolution} • $timeFormatted",
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Quality Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF27272A)
                    ) {
                        Text(
                            text = "HD 60FPS",
                            color = Color(0xFF60A5FA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Sharing Canvas Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF09090B))
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (session.isPrivacyShieldActive) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🛡️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Privacy Shield Active",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Display is hidden from participants for security",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else if (session.isPaused) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⏸️", fontSize = 44.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Screen Broadcast Paused",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Tap 'Resume' below to continue streaming",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // Simulated device broadcast preview stream
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Simulated App Viewport
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF18181B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "📱", fontSize = 46.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Broadcasting Mobile Screen",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Active streaming pipeline: MediaProjection • Low Latency",
                                    color = Color(0xFF34D399),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${session.targetUserName} is viewing your screen",
                                    color = Color(0xFFA1A1AA),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control Bar
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF18181B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Audio toggle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onToggleAudio,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (session.isAudioSharing) Color(0xFF27272A) else Color(0xFF3F3F46))
                        ) {
                            Icon(
                                imageVector = if (session.isAudioSharing) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Audio Toggle",
                                tint = if (session.isAudioSharing) Color(0xFF34D399) else Color(0xFFF87171)
                            )
                        }
                        Text(
                            text = if (session.isAudioSharing) "Audio On" else "Audio Muted",
                            color = Color(0xFFA1A1AA),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Privacy Shield toggle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onTogglePrivacyShield,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (session.isPrivacyShieldActive) Color(0xFF6366F1) else Color(0xFF27272A))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Privacy Shield",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = if (session.isPrivacyShieldActive) "Shielded" else "Shield",
                            color = Color(0xFFA1A1AA),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Pause / Resume toggle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onTogglePause,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (session.isPaused) Color(0xFFF59E0B) else Color(0xFF27272A))
                        ) {
                            Icon(
                                imageVector = if (session.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = if (session.isPaused) "Resume" else "Pause",
                            color = Color(0xFFA1A1AA),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Stop Screen Share Button
                    Button(
                        onClick = onStopScreenShare,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.StopScreenShare,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop Sharing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
