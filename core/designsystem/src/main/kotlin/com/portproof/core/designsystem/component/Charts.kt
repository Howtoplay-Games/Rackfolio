package com.portproof.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/**
 * Dense bar chart of thin rounded bars (reference "Heart Rate"). Bars at [highlighted] indices use
 * the peak colour. [axisLabels] are spread evenly under the chart.
 */
@Composable
fun PpBarChart(
    values: List<Int>,
    highlighted: Set<Int>,
    axisLabels: List<String>,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    height: Dp = 96.dp,
) {
    val colors = PortProofTheme.colors
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(values) { reveal.snapTo(0f); reveal.animateTo(1f, tween(durationMillis = 650)) }
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    Column(modifier.clearAndSetSemantics { contentDescription = accessibilityLabel }) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            if (values.isEmpty()) return@Canvas
            val slot = size.width / values.size
            val barWidth = (slot * 0.55f).coerceAtMost(6.dp.toPx())
            val minHeight = 6.dp.toPx()
            values.forEachIndexed { index, value ->
                val fraction = value.toFloat() / max
                val barHeight = (minHeight + (size.height - minHeight) * fraction) * reveal.value
                val color = when {
                    index in highlighted -> colors.chartPeak
                    value == 0 -> colors.chartBar.copy(alpha = 0.45f)
                    else -> colors.chartBar
                }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(index * slot + (slot - barWidth) / 2, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            axisLabels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary) }
        }
    }
}

/** Thin rounded progress bar (reference "Hydration"). */
@Composable
fun PpProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = PortProofTheme.colors.chartWater,
    trackColor: Color = PortProofTheme.colors.chartWaterTrack,
    height: Dp = 8.dp,
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(target, tween(durationMillis = 650), label = "progress")
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) },
    ) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(trackColor, cornerRadius = radius)
        if (animated > 0f) drawRoundRect(color, size = Size(size.width * animated, size.height), cornerRadius = radius)
    }
}

data class PpRingDay(val label: String, val progress: Float)

/** A row of small progress rings with day initials underneath (reference "Hydration"). */
@Composable
fun PpRingRow(days: List<PpRingDay>, accessibilityLabel: String, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = accessibilityLabel },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        days.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(32.dp)) {
                        val stroke = 3.5.dp.toPx()
                        val inset = stroke / 2
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(colors.chartWaterTrack, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                        if (day.progress > 0f) {
                            drawArc(
                                colors.chartWater, -90f, 360f * day.progress.coerceIn(0f, 1f), false,
                                Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
                            )
                        }
                    }
                }
                Text(
                    day.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
