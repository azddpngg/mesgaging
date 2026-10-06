package com.example.mesgaging.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mesgaging.model.UserContact
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.AppBg
import com.example.ui.theme.DividerColor
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceHover
import com.example.ui.theme.SurfaceInput
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QrScannerDialog(
    onDismiss: () -> Unit,
    onQrPayloadScanned: (String) -> Unit,
    onOpenDm: (UserContact) -> Unit
) {
    var rawInputText by remember { mutableStateOf("") }
    var connectionError by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current

    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
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
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connect via QR Code",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clean Scanner Viewfinder
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppBg)
                        .border(1.dp, DividerColor, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 3.5f
                        val cornerLen = 28f
                        val cornerColor = Color.White

                        // Top-Left
                        drawLine(cornerColor, Offset(20f, 20f), Offset(20f + cornerLen, 20f), stroke)
                        drawLine(cornerColor, Offset(20f, 20f), Offset(20f, 20f + cornerLen), stroke)
                        // Top-Right
                        drawLine(cornerColor, Offset(size.width - 20f, 20f), Offset(size.width - 20f - cornerLen, 20f), stroke)
                        drawLine(cornerColor, Offset(size.width - 20f, 20f), Offset(size.width - 20f, 20f + cornerLen), stroke)
                        // Bottom-Left
                        drawLine(cornerColor, Offset(20f, size.height - 20f), Offset(20f + cornerLen, size.height - 20f), stroke)
                        drawLine(cornerColor, Offset(20f, size.height - 20f), Offset(20f, size.height - 20f - cornerLen), stroke)
                        // Bottom-Right
                        drawLine(cornerColor, Offset(size.width - 20f, size.height - 20f), Offset(size.width - 20f - cornerLen, size.height - 20f), stroke)
                        drawLine(cornerColor, Offset(size.width - 20f, size.height - 20f), Offset(size.width - 20f, size.height - 20f - cornerLen), stroke)

                        // Subtle scan laser line
                        val laserY = 24f + (size.height - 48f) * laserProgress
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = Offset(24f, laserY),
                            end = Offset(size.width - 24f, laserY),
                            strokeWidth = 2.5f
                        )
                    }

                    Text(
                        text = "Align QR code inside frame",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Or paste invite code / payload:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = {
                        rawInputText = it
                        connectionError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Paste invite payload or code...", color = TextMuted, fontSize = 12.sp) },
                    trailingIcon = {
                        IconButton(onClick = {
                            val text = clipboardManager.getText()?.text
                            if (!text.isNullOrBlank()) {
                                rawInputText = text
                            }
                        }) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Paste", tint = TextSecondary)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceInput,
                        unfocusedContainerColor = SurfaceInput,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                if (connectionError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = connectionError!!,
                        color = Color(0xFFED4245),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (rawInputText.isNotBlank()) {
                            onQrPayloadScanned(rawInputText.trim())
                            onDismiss()
                        } else {
                            connectionError = "Please enter or paste a valid QR invite"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Connect Contact", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
