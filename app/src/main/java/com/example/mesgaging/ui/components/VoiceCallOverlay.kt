package com.example.mesgaging.ui.components

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.StopScreenShare
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesgaging.model.ActiveCall
import com.example.mesgaging.model.CallSoundboardEffect
import com.example.mesgaging.model.CallState
import com.example.mesgaging.model.CallVoiceEffect
import com.example.ui.theme.AccentDanger
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DividerColor
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceHover
import com.example.ui.theme.SurfaceNav
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun VoiceCallOverlay(
    call: ActiveCall,
    micAmplitude: Float = 0f,
    liveScreenBitmap: Bitmap? = null,
    isScreenSharePaused: Boolean = false,
    screenShareResolution: String = "1080p • 60 FPS",
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleScreenShare: (Activity) -> Unit,
    onToggleScreenSharePause: () -> Unit,
    onSelectVoiceEffect: (CallVoiceEffect) -> Unit,
    onPlaySoundboardEffect: (CallSoundboardEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var callSeconds by remember { mutableLongStateOf(0L) }
    var selectedEffectTab by remember { mutableIntStateOf(0) } // 0 = Voice Filters, 1 = Soundboard

    LaunchedEffect(call.state) {
        if (call.state == CallState.CONNECTED) {
            while (true) {
                delay(1000)
                callSeconds++
            }
        }
    }

    val minutes = callSeconds / 60
    val seconds = callSeconds % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    val dynamicPulse = (1.0f + micAmplitude * 0.45f).coerceIn(1.0f, 1.55f)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val liveBadgeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_badge"
    )

    // True OLED Deep Black Canvas
    Surface(
        color = Color(0xFF000000),
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("voice_call_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP STATUS BAR
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF08080A),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1C1C20)),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (call.isScreenSharing) Color(0xFFEF4444).copy(alpha = liveBadgeAlpha) else AccentSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (call.isScreenSharing) "LIVE SCREEN SHARE • $durationText" else "VOICE CALL • $durationText",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // CENTER: SCREEN SHARING STREAM OR PARTICIPANT AVATAR
            if (call.isScreenSharing) {
                // REAL LIVE IN-CALL SCREEN SHARING VIEWPORT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF050506))
                        .border(1.dp, Color(0xFF1E1E24), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScreenSharePaused) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "⏸️", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Screen Broadcast Paused",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Tap play button below to resume streaming",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else if (liveScreenBitmap != null) {
                        // Render Actual Live Captured Screen Bitmap
                        Image(
                            bitmap = liveScreenBitmap.asImageBitmap(),
                            contentDescription = "Live Shared Screen",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        // Initializing or Waiting for first frame
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "📱", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Broadcasting Mobile Screen Live",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$screenShareResolution • Low Latency Stream",
                                color = AccentSuccess,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Top Streaming Overlay Pill
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2C2C34))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${call.participantName} viewing",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Mini In-Call Audio PiP Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF101014).copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFF2E2E38), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = call.participantEmoji, fontSize = 20.sp)
                    }

                    // Pause / Resume Control
                    IconButton(
                        onClick = onToggleScreenSharePause,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(0.5.dp, Color(0xFF2C2C34), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isScreenSharePaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Toggle Pause",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // NORMAL VOICE CALL DISPLAY
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Outer Pulsing Ring responding to real voice volume
                        if (call.state == CallState.CONNECTED) {
                            Box(
                                modifier = Modifier
                                    .size(136.dp)
                                    .scale(dynamicPulse)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            )
                        }

                        // Participant Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF08080A))
                                .border(1.5.dp, Color(0xFF222228), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = call.participantEmoji,
                                fontSize = 52.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = call.participantName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when {
                            call.isMuted -> "Microphone Muted"
                            micAmplitude > 0.1f -> "Speaking (${call.activeVoiceEffect.displayName})..."
                            else -> "Connected • HD Voice (${call.activeVoiceEffect.displayName})"
                        },
                        color = if (micAmplitude > 0.1f) AccentSuccess else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Audio Level Equalizer Bars
                    if (call.state == CallState.CONNECTED) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 5) {
                                val barHeight = (6 + (micAmplitude * (18 + i * 3))).dp
                                Box(
                                    modifier = Modifier
                                        .width(3.5.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (call.isMuted) TextMuted else AccentSuccess)
                                )
                            }
                        }
                    }
                }
            }

            // CALL EFFECTS & SOUNDBOARD (Clean, functional, NO "pixel voice effect" text)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab Selector: Voice Filters vs Soundboard
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0A0A0C),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1C1C20)),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Voice Filters Tab
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedEffectTab == 0) Color(0xFF1E1E24) else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedEffectTab = 0 }
                        ) {
                            Text(
                                text = "VOICE FILTERS",
                                color = if (selectedEffectTab == 0) Color.White else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Soundboard Tab
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedEffectTab == 1) Color(0xFF1E1E24) else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedEffectTab = 1 }
                        ) {
                            Text(
                                text = "SOUNDBOARD",
                                color = if (selectedEffectTab == 1) Color.White else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Effect Content
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF08080A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1A1A1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedEffectTab == 0) {
                        // Live DSP Voice Modulators
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(CallVoiceEffect.entries.toTypedArray()) { effect ->
                                val isSelected = call.activeVoiceEffect == effect
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) Color(0xFF1A1A22) else Color(0xFF0F0F12),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 1.5.dp else 0.5.dp,
                                        if (isSelected) Color.White else Color(0xFF1E1E24)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSelectVoiceEffect(effect) }
                                        .testTag("voice_effect_${effect.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = effect.iconEmoji, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = effect.displayName,
                                                color = if (isSelected) Color.White else TextSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = effect.description,
                                                color = TextMuted,
                                                fontSize = 9.sp,
                                                maxLines = 1
                                            )
                                        }
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Real Reaction Soundboard
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CallSoundboardEffect.entries.forEach { sound ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onPlaySoundboardEffect(sound) }
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                        .testTag("call_sound_${sound.name.lowercase()}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF141418))
                                            .border(1.dp, Color(0xFF222228), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = sound.emoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sound.label,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // IN-CALL ACTION DOCK: MUTE, SCREEN SHARE, END CALL, SPEAKER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Mute Mic Toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (call.isMuted) Color.White else Color(0xFF0F0F12))
                        .border(1.dp, Color(0xFF222228), CircleShape)
                        .testTag("mute_mic_button")
                ) {
                    Icon(
                        imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (call.isMuted) Color.Black else TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. IN-CALL SCREEN SHARING TOGGLE
                IconButton(
                    onClick = {
                        if (activity != null) {
                            onToggleScreenShare(activity)
                        }
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (call.isScreenSharing) Color.White else Color(0xFF0F0F12))
                        .border(
                            1.dp,
                            if (call.isScreenSharing) Color.White else Color(0xFF222228),
                            CircleShape
                        )
                        .testTag("in_call_screen_share_button")
                ) {
                    Icon(
                        imageVector = if (call.isScreenSharing) Icons.AutoMirrored.Filled.StopScreenShare else Icons.AutoMirrored.Filled.ScreenShare,
                        contentDescription = "In-Call Screen Share",
                        tint = if (call.isScreenSharing) Color.Black else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 3. End Call Button (Large Red)
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(AccentDanger)
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 4. Speakerphone Toggle
                IconButton(
                    onClick = onToggleSpeaker,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (call.isSpeakerOn) Color(0xFF25252E) else Color(0xFF0F0F12))
                        .border(1.dp, Color(0xFF222228), CircleShape)
                        .testTag("toggle_speaker_button")
                ) {
                    Icon(
                        imageVector = if (call.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = if (call.isSpeakerOn) Color.White else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
