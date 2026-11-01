package com.portproof.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

data class PpChartPoint(val label: String, val value: Int, val tooltip: String)

/**
 * Smooth single-series line chart with horizontal grid lines and a tap-to-inspect tooltip.
 * The whole chart is exposed to accessibility services as one [accessibilityLabel].
 */
@Composable
fun PpLineChart(
    points: List<PpChartPoint>,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return
    val colors = PortProofTheme.colors
    val captionStyle = PortProofTheme.numbers.caption
    val textMeasurer = rememberTextMeasurer()
    var selected by rememberSaveable(points.size) { mutableIntStateOf(points.lastIndex) }
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(points) { reveal.snapTo(0f); reveal.animateTo(1f, tween(durationMillis = 700)) }
    val maxValue = maxOf(points.maxOf { it.value }, 4)

    Column(modifier.clearAndSetSemantics { contentDescription = accessibilityLabel }) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val step = size.width.toFloat() / (points.size - 1).coerceAtLeast(1)
                        selected = (offset.x / step).let { kotlin.math.round(it).toInt() }.coerceIn(0, points.lastIndex)
                    }
                },
        ) {
            val top = 34.dp.toPx()
            val chartHeight = size.height - top - 4.dp.toPx()
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            fun pointAt(i: Int) = Offset(
                x = i * stepX,
                y = top + chartHeight * (1f - points[i].value.toFloat() / maxValue * reveal.value),
            )
            repeat(4) { line ->
                val y = top + chartHeight * line / 3f
                drawLine(colors.divider, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            val path = smoothPath(points.indices.map(::pointAt))
            val fill = Path().apply {
                addPath(path)
                lineTo(size.width, top + chartHeight)
                lineTo(0f, top + chartHeight)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(colors.accent.copy(alpha = 0.14f), colors.accent.copy(alpha = 0f))))
            drawPath(path, colors.accent, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            drawSelection(
                point = pointAt(selected),
                tooltip = points[selected].tooltip,
                top = top,
                chartHeight = chartHeight,
                textMeasurer = textMeasurer,
                style = captionStyle.copy(color = colors.onInverseSurface),
                inverse = colors.inverseSurface,
                halo = colors.surface,
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            points.forEachIndexed { index, point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == selected) colors.textPrimary else colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Monotone-friendly cubic smoothing between consecutive points. */
private fun smoothPath(points: List<Offset>): Path = Path().apply {
    moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        val previous = points[i - 1]
        val current = points[i]
        val midX = (previous.x + current.x) / 2f
        cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
    }
}

private fun DrawScope.drawSelection(
    point: Offset,
    tooltip: String,
    top: Float,
    chartHeight: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    style: androidx.compose.ui.text.TextStyle,
    inverse: androidx.compose.ui.graphics.Color,
    halo: androidx.compose.ui.graphics.Color,
) {
    drawLine(inverse.copy(alpha = 0.35f), Offset(point.x, top), Offset(point.x, top + chartHeight), 1.dp.toPx())
    drawCircle(halo, radius = 5.dp.toPx(), center = point)
    drawCircle(inverse, radius = 3.5.dp.toPx(), center = point)

    val layout = textMeasurer.measure(tooltip, style)
    val padH = 8.dp.toPx()
    val padV = 5.dp.toPx()
    val boxSize = Size(layout.size.width + padH * 2, layout.size.height + padV * 2)
    val left = (point.x - boxSize.width / 2).coerceIn(0f, size.width - boxSize.width)
    drawRoundRect(inverse, Offset(left, 0f), boxSize, CornerRadius(6.dp.toPx()))
    drawText(layout, topLeft = Offset(left + padH, padV))
}
