package com.portproof.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme

data class PpAreaPoint(val axisLabel: String, val value: Int)

/**
 * Dark summary card with a smooth line chart (reference "Health Metrics Summary"). The area up to
 * the selected point is filled with the accent gradient; tapping the chart moves the selection.
 */
@Composable
fun PpAreaChartCard(
    caption: String,
    value: String,
    unit: String,
    points: List<PpAreaPoint>,
    accessibilityLabel: String,
    infoLabel: String,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PortProofTheme.colors
    var selected by rememberSaveable(points.size) { mutableIntStateOf(points.lastIndex.coerceAtLeast(0)) }
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(points) { reveal.snapTo(0f); reveal.animateTo(1f, tween(durationMillis = 800)) }
    Surface(modifier = modifier.fillMaxWidth(), shape = PortProofTheme.shapes.hero, color = colors.hero) {
        Column(Modifier.padding(top = 18.dp, bottom = 18.dp)) {
            Row(Modifier.padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(caption, style = MaterialTheme.typography.bodySmall, color = colors.onHeroMuted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(value, style = PortProofTheme.numbers.metricLarge, color = colors.onHero)
                        Spacer(Modifier.width(6.dp))
                        Text(unit, style = MaterialTheme.typography.labelMedium, color = colors.onHeroMuted, modifier = Modifier.padding(bottom = 6.dp))
                    }
                }
                IconButton(onClick = onInfoClick) {
                    Icon(PortProofIcons.Info, contentDescription = infoLabel, tint = colors.onHero)
                }
            }
            if (points.size >= 2) {
                AreaChart(points, selected, reveal.value, accessibilityLabel) { selected = it }
                AxisLabels(points, selected)
            }
        }
    }
}

@Composable
private fun AreaChart(points: List<PpAreaPoint>, selected: Int, reveal: Float, accessibilityLabel: String, onSelect: (Int) -> Unit) {
    val colors = PortProofTheme.colors
    val max = (points.maxOf { it.value }).coerceAtLeast(4)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(top = 12.dp, start = 14.dp, end = 14.dp)
            .clearAndSetSemantics { contentDescription = accessibilityLabel }
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val step = size.width.toFloat() / (points.size - 1)
                    onSelect(kotlin.math.round(offset.x / step).toInt().coerceIn(0, points.lastIndex))
                }
            },
    ) {
        val top = 10.dp.toPx()
        val chartHeight = size.height - top - 2.dp.toPx()
        val stepX = size.width / (points.size - 1)
        val offsets = points.mapIndexed { i, p -> Offset(i * stepX, top + chartHeight * (1f - p.value.toFloat() / max * reveal)) }
        val line = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            for (i in 1 until offsets.size) {
                val midX = (offsets[i - 1].x + offsets[i].x) / 2f
                cubicTo(midX, offsets[i - 1].y, midX, offsets[i].y, offsets[i].x, offsets[i].y)
            }
        }
        val area = Path().apply {
            addPath(line)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        val selectedPoint = offsets[selected]
        clipRect(right = selectedPoint.x) {
            drawPath(area, Brush.verticalGradient(listOf(colors.accent, colors.accent.copy(alpha = 0.15f))))
        }
        drawPath(line, colors.onHero, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        drawLine(colors.onHero.copy(alpha = 0.5f), Offset(selectedPoint.x, selectedPoint.y), Offset(selectedPoint.x, size.height), 1.dp.toPx())
        drawCircle(colors.onHero, radius = 6.dp.toPx(), center = selectedPoint)
        drawCircle(colors.accent, radius = 3.5.dp.toPx(), center = selectedPoint)
    }
}

@Composable
private fun AxisLabels(points: List<PpAreaPoint>, selected: Int) {
    val colors = PortProofTheme.colors
    Canvas(Modifier.fillMaxWidth().height(10.dp).padding(top = 4.dp, start = 14.dp, end = 14.dp)) {
        val ticks = (points.size - 1) * 4
        repeat(ticks + 1) { i ->
            val x = size.width * i / ticks
            val tall = i % 4 == 0
            drawLine(colors.onHeroMuted.copy(alpha = if (tall) 0.9f else 0.45f), Offset(x, 0f), Offset(x, if (tall) size.height else size.height * 0.6f), 1.dp.toPx())
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 6.dp, start = 4.dp, end = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        points.forEachIndexed { index, point ->
            val isSelected = index == selected
            Box(
                Modifier
                    .background(if (isSelected) colors.heroTrack else androidx.compose.ui.graphics.Color.Transparent, PortProofTheme.shapes.pill)
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(point.axisLabel, style = MaterialTheme.typography.labelSmall, color = if (isSelected) colors.onHero else colors.onHeroMuted, textAlign = TextAlign.Center)
            }
        }
    }
}
