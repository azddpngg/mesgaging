package com.example.mesgaging.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest

object QrCodeGenerator {

    /**
     * Generates a 25x25 QR Matrix (Version 2 layout) with authentic finder patterns,
     * timing strips, separators, and hashed data payload modules.
     */
    fun generateMatrix(payload: String, matrixSize: Int = 25): Array<BooleanArray> {
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) { false } }
        val reserved = Array(matrixSize) { BooleanArray(matrixSize) { false } }

        fun placeFinderPattern(rowStart: Int, colStart: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val row = rowStart + r
                    val col = colStart + c
                    if (row in 0 until matrixSize && col in 0 until matrixSize) {
                        reserved[row][col] = true
                        val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                        val isCenter = r in 2..4 && c in 2..4
                        matrix[row][col] = isBorder || isCenter
                    }
                }
            }
        }

        // 1. Top-Left Finder
        placeFinderPattern(0, 0)
        // 2. Top-Right Finder
        placeFinderPattern(0, matrixSize - 7)
        // 3. Bottom-Left Finder
        placeFinderPattern(matrixSize - 7, 0)

        // Separators around finders
        for (i in 0 until 8) {
            if (i < matrixSize) {
                reserved[7][i] = true
                reserved[i][7] = true
                reserved[7][matrixSize - 1 - i] = true
                reserved[i][matrixSize - 8] = true
                reserved[matrixSize - 8][i] = true
                reserved[matrixSize - 1 - i][7] = true
            }
        }

        // Timing patterns (row 6 and col 6)
        for (i in 8 until matrixSize - 8) {
            reserved[6][i] = true
            matrix[6][i] = (i % 2 == 0)
            reserved[i][6] = true
            matrix[i][6] = (i % 2 == 0)
        }

        // Alignment pattern at (matrixSize - 9, matrixSize - 9)
        val alignR = matrixSize - 9
        val alignC = matrixSize - 9
        for (r in -2..2) {
            for (c in -2..2) {
                val row = alignR + r
                val col = alignC + c
                if (row in 0 until matrixSize && col in 0 until matrixSize) {
                    reserved[row][col] = true
                    val isBorder = r == -2 || r == 2 || c == -2 || c == 2
                    val isCenter = r == 0 && c == 0
                    matrix[row][col] = isBorder || isCenter
                }
            }
        }

        // Deterministic bitstream expansion using SHA-256 for rich data layout
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(payload.toByteArray(Charsets.UTF_8))
        var byteIndex = 0
        var bitIndex = 0

        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (!reserved[r][c]) {
                    val currentByte = hash[byteIndex % hash.size].toInt()
                    val bit = (currentByte shr bitIndex) and 1
                    matrix[r][c] = (bit == 1) xor ((r + c) % 3 == 0)

                    bitIndex++
                    if (bitIndex >= 8) {
                        bitIndex = 0
                        byteIndex++
                    }
                }
            }
        }

        return matrix
    }

    fun buildPayload(id: String, username: String, discriminator: String, emoji: String, colorHex: Long): String {
        return "mesgaging://connect?id=$id&u=$username&d=$discriminator&e=$emoji&c=$colorHex"
    }

    fun parsePayload(payload: String): QrPayloadData? {
        if (!payload.startsWith("mesgaging://connect?")) return null
        val query = payload.substringAfter("mesgaging://connect?")
        val params = query.split("&").associate {
            val parts = it.split("=")
            if (parts.size == 2) parts[0] to parts[1] else "" to ""
        }

        val id = params["id"] ?: return null
        val username = params["u"] ?: "UnknownUser"
        val discriminator = params["d"] ?: "#0000"
        val emoji = params["e"] ?: "👾"
        val colorHex = params["c"]?.toLongOrNull() ?: 0xFF5865F2

        return QrPayloadData(
            id = id,
            username = username,
            discriminator = discriminator,
            avatarEmoji = emoji,
            avatarColor = colorHex
        )
    }
}

data class QrPayloadData(
    val id: String,
    val username: String,
    val discriminator: String,
    val avatarEmoji: String,
    val avatarColor: Long
)

@Composable
fun QrCodeView(
    payload: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 220.dp,
    dotColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    centerEmoji: String? = null
) {
    val matrix = remember(payload) { QrCodeGenerator.generateMatrix(payload) }
    val matrixSize = matrix.size

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val moduleWidth = size.width / matrixSize
            val moduleHeight = size.height / matrixSize
            val cornerRadius = moduleWidth * 0.25f

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r][c]) {
                        drawRoundRect(
                            color = dotColor,
                            topLeft = Offset(c * moduleWidth, r * moduleHeight),
                            size = Size(moduleWidth * 0.94f, moduleHeight * 0.94f),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                        )
                    }
                }
            }
        }

        // Center Avatar Badge inside QR
        if (centerEmoji != null) {
            Box(
                modifier = Modifier
                    .size(sizeDp * 0.22f)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2024)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = centerEmoji,
                    fontSize = (sizeDp.value * 0.1f).sp
                )
            }
        }
    }
}
