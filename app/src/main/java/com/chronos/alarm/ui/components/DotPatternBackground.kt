package com.chronos.alarm.ui.components

// Phase 2: Design System - Dot Pattern Background
// Draws radial gradient dot pattern matching React Background.tsx
// 24px spacing, 40% opacity, uses Canvas for performance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun DotPatternBackground(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.Black.copy(alpha = 0.1f),
    dotSize: Float = 2f,
    spacing: Float = 24f
) {
    val density = LocalDensity.current

    Canvas(modifier = modifier.fillMaxSize()) {
        drawDotPattern(dotColor, dotSize, spacing, density)
    }
}

private fun DrawScope.drawDotPattern(
    dotColor: Color,
    dotSize: Float,
    spacing: Float,
    density: androidx.compose.ui.unit.Density
) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val dotsX = (canvasWidth / spacing).toInt() + 1
    val dotsY = (canvasHeight / spacing).toInt() + 1

    val paint = android.graphics.Paint().apply {
        color = dotColor.copy(alpha = 0.4f).hashCode()
        isAntiAlias = true
    }

    for (x in 0 until dotsX) {
        for (y in 0 until dotsY) {
            val posX = x * spacing
            val posY = y * spacing
            drawCircle(
                color = dotColor,
                radius = dotSize,
                center = Offset(posX, posY)
            )
        }
    }
}