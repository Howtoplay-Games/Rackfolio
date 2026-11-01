package com.portproof.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Number of segments that represent [fraction]: never empty above 0, never full below 1. */
fun gaugeFilledSegments(fraction: Float, segments: Int): Int = when {
    fraction <= 0f -> 0
    fraction >= 1f -> segments
    else -> (fraction * segments).roundToInt().coerceIn(1, segments - 1)
}

/**
 * The reference dashboard's horseshoe of rounded segments, open at the bottom, with text in the
 * middle. The figure in [content] carries the meaning; [description] is what screen readers hear.
 */
@Composable
fun PpSegmentedGauge(
    fraction: Float,
    description: String,
    modifier: Modifier = Modifier,
    segments: Int = 11,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PortProofTheme.colors
    val filled = gaugeFilledSegments(fraction, segments)
    BoxWithConstraints(
        modifier
            .widthIn(max = 340.dp)
            .fillMaxWidth()
            .aspectRatio(1.3f)
            .clearAndSetSemantics {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(fraction.coerceIn(0f, 1f), 0f..1f)
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val sweep = 240f
            val step = sweep / (segments - 1)
            val outer = min(size.width / 2f, size.height / 1.62f)
            val length = outer * 0.34f
            val radius = outer - length / 2f
            val thickness = radius * 0.30f
            val center = Offset(size.width / 2f, outer + (size.height - outer * 1.62f) / 2f)
            repeat(segments) { i ->
                val angle = 210f - i * step
                val radians = Math.toRadians(angle.toDouble())
                val segmentCenter = Offset(
                    center.x + radius * cos(radians).toFloat(),
                    center.y - radius * sin(radians).toFloat(),
                )
                rotate(degrees = 90f - angle, pivot = segmentCenter) {
                    drawRoundRect(
                        color = if (i < filled) colors.accent else colors.accentTrack,
                        topLeft = Offset(segmentCenter.x - thickness / 2f, segmentCenter.y - length / 2f),
                        size = Size(thickness, length),
                        cornerRadius = CornerRadius(thickness * 0.42f),
                    )
                }
            }
        }
        // The arc's centre sits below the middle of the box; shift the text down to match it.
        Column(
            Modifier.padding(top = maxWidth * 0.17f),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}
