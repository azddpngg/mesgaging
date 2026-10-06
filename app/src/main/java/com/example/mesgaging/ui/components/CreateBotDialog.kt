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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mesgaging.model.MessageEffect
import com.example.mesgaging.model.UserBot
import com.example.ui.theme.DividerColor
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceHover
import com.example.ui.theme.SurfaceInput
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateBotDialog(
    onDismiss: () -> Unit,
    onBotCreated: (UserBot) -> Unit
) {
    var botName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🤖") }
    var triggerPrefix by remember { mutableStateOf("!") }
    var promptInstruction by remember { mutableStateOf("") }
    var selectedEffects by remember { mutableStateOf(setOf<MessageEffect>()) }

    val presetTemplates = listOf(
        "Candid Rebel" to "Unfiltered, witty, highly candid, speaks directly without corporate disclaimers or sugarcoating.",
        "Roast Master" to "Playfully roasts everything the user says with brutal humor and savage wit.",
        "Code Hacker" to "Technical genius, writes clean code, answers architectural and coding questions directly.",
        "Hype Demon" to "All caps energy, screams hype, loves explosions, bass boost, and extreme chaos!"
    )

    val emojiPicks = listOf("🤖", "👾", "🧙", "💀", "🧠", "⚡", "🥷", "🐺", "👑", "🎯", "💎", "🐉")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceHover),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Bot",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Create Custom Bot",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Build your own custom AI companion",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar and Name Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SurfaceInput)
                            .border(1.dp, DividerColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedEmoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = botName,
                        onValueChange = { botName = it },
                        label = { Text("Bot Name") },
                        placeholder = { Text("e.g. ChaosBot, RoastMaster...", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Emoji Selection
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    emojiPicks.forEach { emoji ->
                        val isPicked = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isPicked) Color.White.copy(alpha = 0.2f) else SurfaceHover)
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Persona Templates
                Text(
                    text = "Persona Template / Preset:",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetTemplates.forEach { (label, prompt) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceHover)
                                .clickable { promptInstruction = prompt }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = label, color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom System Instruction / Persona
                OutlinedTextField(
                    value = promptInstruction,
                    onValueChange = { promptInstruction = it },
                    label = { Text("Bot Personality & Instructions") },
                    placeholder = { Text("How should this bot talk, act, or reply?", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Trigger Prefix
                OutlinedTextField(
                    value = triggerPrefix,
                    onValueChange = { triggerPrefix = it.take(3) },
                    label = { Text("Trigger Prefix (in channels)") },
                    placeholder = { Text("e.g. !, /, @", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Default Reply Effects
                Text(
                    text = "Auto-Attach Effects on Reply:",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        MessageEffect.EXPLOSION,
                        MessageEffect.BASS_BOOST,
                        MessageEffect.EARTHQUAKE,
                        MessageEffect.TTS,
                        MessageEffect.GLITCH,
                        MessageEffect.INFERNO,
                        MessageEffect.SLAM,
                        MessageEffect.FROST
                    ).forEach { effect ->
                        val isChecked = selectedEffects.contains(effect)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChecked) Color.White.copy(alpha = 0.2f) else SurfaceHover)
                                .border(1.dp, if (isChecked) Color.White else DividerColor, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedEffects = if (isChecked) selectedEffects - effect else selectedEffects + effect
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${effect.iconEmoji} ${effect.displayName}",
                                color = if (isChecked) TextPrimary else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceHover)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (botName.isNotBlank()) {
                                val bot = UserBot(
                                    id = "bot_${UUID.randomUUID()}",
                                    name = botName.trim(),
                                    avatarEmoji = selectedEmoji.ifBlank { "🤖" },
                                    rolePrompt = promptInstruction.trim().ifBlank { "You are $botName. Speak candidly and directly." },
                                    triggerPrefix = triggerPrefix.ifBlank { "!" },
                                    defaultEffects = selectedEffects
                                )
                                onBotCreated(bot)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Create Bot", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
