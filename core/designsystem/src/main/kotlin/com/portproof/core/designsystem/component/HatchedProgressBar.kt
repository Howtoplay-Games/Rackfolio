package com.portproof.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Progress bar whose remaining portion is drawn with diagonal hatching. */
@Composable
fun PpHatchedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = PortProofTheme.colors.accent,
    height: Dp = 10.dp,
) {
    val hatch = PortProofTheme.colors.border
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(target, tween(durationMillis = 600), label = "progress")
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(PortProofTheme.shapes.pill)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) },
    ) {
        val filled = size.width * animated
        val radius = CornerRadius(size.height / 2, size.height / 2)
        clipRect(left = filled) {
            val step = 6.dp.toPx()
            val stroke = 1.5.dp.toPx()
            var x = filled - size.height
            while (x < size.width) {
                drawLine(hatch, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = stroke)
                x += step
            }
        }
        if (filled > 0f) drawRoundRect(color, size = Size(filled, size.height), cornerRadius = radius)
    }
}
