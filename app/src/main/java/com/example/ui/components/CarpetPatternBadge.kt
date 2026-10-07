package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CarpetPatternBadge(
    patternStyle: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val (bgColor, accentColor) = when (patternStyle.lowercase()) {
        "klasyczny" -> Color(0xFF7F1D1D) to Color(0xFFFDE047)
        "nowoczesny" -> Color(0xFF0F766E) to Color(0xFF5EEAD4)
        "geometryczny" -> Color(0xFF1E3A8A) to Color(0xFFF59E0B)
        "shaggy" -> Color(0xFF78350F) to Color(0xFFFDE68A)
        "boho" -> Color(0xFF713F12) to Color(0xFFE2E8F0)
        "vintage" -> Color(0xFF4C1D95) to Color(0xFFC4B5FD)
        "dziecięcy" -> Color(0xFF0284C7) to Color(0xFFF472B6)
        else -> Color(0xFF334155) to Color(0xFF94A3B8)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.8f)) {
            val w = this.size.width
            val h = this.size.height

            when (patternStyle.lowercase()) {
                "klasyczny" -> {
                    // Persian medalion
                    drawRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.15f, h * 0.15f),
                        size = Size(w * 0.7f, h * 0.7f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = accentColor,
                        radius = w * 0.22f,
                        center = Offset(w / 2, h / 2)
                    )
                }
                "geometryczny" -> {
                    // Diamonds & diagonals
                    val path = Path().apply {
                        moveTo(w / 2, h * 0.1f)
                        lineTo(w * 0.9f, h / 2)
                        lineTo(w / 2, h * 0.9f)
                        lineTo(w * 0.1f, h / 2)
                        close()
                    }
                    drawPath(path, color = accentColor, style = Stroke(width = 2.dp.toPx()))
                    drawLine(
                        color = accentColor,
                        start = Offset(w * 0.1f, h * 0.1f),
                        end = Offset(w * 0.9f, h * 0.9f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                "shaggy" -> {
                    // Fluffy wavy lines
                    for (i in 1..4) {
                        drawLine(
                            color = accentColor,
                            start = Offset(w * 0.15f, h * (i * 0.2f)),
                            end = Offset(w * 0.85f, h * (i * 0.2f)),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
                "boho" -> {
                    // Braided stripes
                    drawLine(
                        color = accentColor,
                        start = Offset(w * 0.2f, h * 0.2f),
                        end = Offset(w * 0.8f, h * 0.8f),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = accentColor,
                        start = Offset(w * 0.2f, h * 0.8f),
                        end = Offset(w * 0.8f, h * 0.2f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                else -> {
                    // Modern minimalist arches
                    drawCircle(
                        color = accentColor,
                        radius = w * 0.35f,
                        center = Offset(w / 2, h / 2),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
        }
    }
}
