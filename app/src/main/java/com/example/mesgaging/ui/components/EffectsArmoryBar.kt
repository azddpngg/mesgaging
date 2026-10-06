package com.example.mesgaging.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesgaging.model.MessageEffect
import com.example.ui.theme.DividerColor
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceHover
import com.example.ui.theme.SurfaceInput
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EffectsArmoryBar(
    selectedEffects: Set<MessageEffect>,
    onToggleEffect: (MessageEffect) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    spamMultiplier: Int,
    onMultiplierChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 8 core human, tactile effects (no AI sci-fi jargon)
    val coreEffects = listOf(
        MessageEffect.SHAKE,
        MessageEffect.BASS,
        MessageEffect.TTS,
        MessageEffect.FIRE,
        MessageEffect.FREEZE,
        MessageEffect.LOUD,
        MessageEffect.SPARK,
        MessageEffect.POOP
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceInput)
            .border(width = 0.5.dp, color = DividerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Controls Row (Repeat count + Clear)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Repeat:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                listOf(1, 2, 5, 10).forEach { mult ->
                    val isSelected = spamMultiplier == mult
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color.White else SurfaceHover)
                            .clickable { onMultiplierChange(mult) }
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${mult}x",
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceHover)
                    .clickable {
                        if (selectedEffects.isEmpty()) onSelectAll() else onClearAll()
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (selectedEffects.isEmpty()) "Select All" else "Clear",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Horizontal Effects Strip (Grounded, tactile chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            coreEffects.forEach { effect ->
                val isArmed = selectedEffects.contains(effect)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isArmed) Color.White.copy(alpha = 0.12f) else SurfaceCard)
                        .border(
                            width = 1.dp,
                            color = if (isArmed) Color.White.copy(alpha = 0.45f) else DividerColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleEffect(effect) }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = effect.iconEmoji, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = effect.displayName,
                            color = if (isArmed) TextPrimary else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isArmed) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
