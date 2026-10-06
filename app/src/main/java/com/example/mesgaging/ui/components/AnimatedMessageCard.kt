package com.example.mesgaging.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mesgaging.model.ChatMessage
import com.example.mesgaging.model.MessageEffect
import com.example.ui.theme.DividerColor
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardSelf
import com.example.ui.theme.SurfaceHover
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedMessageCard(
    message: ChatMessage,
    isPrivacyShieldActive: Boolean = false,
    onSaveToVault: (ChatMessage) -> Unit = {},
    onTriggerEffects: (ChatMessage) -> Unit,
    onReactionTap: (messageId: String, emoji: String) -> Unit,
    onSpamCopy: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasFire = message.effects.contains(MessageEffect.FIRE) || message.effects.contains(MessageEffect.INFERNO)
    val hasFreeze = message.effects.contains(MessageEffect.FREEZE) || message.effects.contains(MessageEffect.FROST)
    val hasLoud = message.effects.contains(MessageEffect.LOUD) || message.effects.contains(MessageEffect.MEGA_CHONK)
    val hasSpark = message.effects.contains(MessageEffect.SPARK) || message.effects.contains(MessageEffect.GLITCH)
    val hasPoop = message.effects.contains(MessageEffect.POOP)
    val hasTts = message.effects.contains(MessageEffect.TTS)
    val hasBass = message.effects.contains(MessageEffect.BASS) || message.effects.contains(MessageEffect.BASS_BOOST)

    val isSelf = message.senderId == "user_me"

    val timeString = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    var isUnmasked by remember(isPrivacyShieldActive) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
        ) {
            if (!isSelf) {
                // Sender Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(message.senderColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = message.senderAvatar, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Bubble (Solid, static, clean, easy to read, no annoying bouncing)
            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isSelf) 16.dp else 4.dp,
                            bottomEnd = if (isSelf) 4.dp else 16.dp
                        )
                    )
                    .background(
                        when {
                            hasPoop -> Color(0xFF2C241E)
                            isSelf -> SurfaceCardSelf
                            else -> SurfaceCard
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            hasFire -> Color(0xFFF0803C)
                            hasFreeze -> Color(0xFF80DEEA)
                            hasSpark -> Color(0xFFFFD54F)
                            hasPoop -> Color(0xFF8D6E63)
                            hasBass -> Color(0xFF9E9E9E)
                            else -> DividerColor
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isSelf) 16.dp else 4.dp,
                            bottomEnd = if (isSelf) 4.dp else 16.dp
                        )
                    )
                    .clickable { onTriggerEffects(message) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Header (Sender name + time)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.senderName,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        if (message.senderId.startsWith("bot_")) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceHover
                            ) {
                                Text(
                                    text = "BOT",
                                    color = TextPrimary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeString,
                            color = TextMuted,
                            fontSize = 10.sp
                        )

                        if (hasTts) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "TTS Voice",
                                tint = Color(0xFFFAA81A),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Shared Photo Attachment
                if (!message.photoUri.isNullOrBlank()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                            .clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(message.photoUri)).apply {
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                    ) {
                        AsyncImage(
                            model = message.photoUri,
                            contentDescription = "Shared Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (message.photoUri.startsWith("http")) {
                            Surface(
                                shape = RoundedCornerShape(bottomStart = 8.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = "☁️ Cloud",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Shared File Attachment Card
                if (!message.fileName.isNullOrBlank()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val isCloud = message.fileUri?.startsWith("http") == true
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceHover,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = "File",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.fileName,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (isCloud) "☁️ ${message.fileSize ?: "Cloud Storage"}" else (message.fileSize ?: "File"),
                                    color = if (isCloud) Color(0xFF60A5FA) else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCloud) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                            IconButton(
                                onClick = {
                                    val target = message.fileUri ?: message.photoUri
                                    if (!target.isNullOrBlank()) {
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(target)).apply {
                                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = TextSecondary)
                            }
                        }
                    }
                }

                // Message Text Content
                if (message.text.isNotBlank()) {
                    if (isPrivacyShieldActive && !isUnmasked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0D1712),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { isUnmasked = true }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Shielded",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Confidential • Tap to unmask",
                                    color = Color(0xFF86EFAC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Text(
                            text = message.text,
                            color = TextPrimary,
                            fontSize = if (hasLoud) 17.sp else 14.sp,
                            fontWeight = if (hasLoud) FontWeight.Bold else FontWeight.Normal,
                            lineHeight = if (hasLoud) 23.sp else 19.sp
                        )
                    }
                }

                // Active Perks Pill Badges (Compact & clean)
                if (message.effects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        message.effects.take(4).forEach { eff ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceHover
                            ) {
                                Text(
                                    text = "${eff.iconEmoji} ${eff.displayName}",
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Reactions Bar
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        message.reactions.forEach { (emoji, count) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceHover,
                                modifier = Modifier.clickable { onReactionTap(message.id, emoji) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = emoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "$count", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Message Quick Actions Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Emoji Reaction Tap
                    listOf("🔥", "💥", "💩", "😂").forEach { em ->
                        Text(
                            text = em,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onReactionTap(message.id, em) }
                                .padding(2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onSpamCopy(message) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Spam Repeat",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
