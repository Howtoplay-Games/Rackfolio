package com.portproof.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Semicircular gauge with a centred value, e.g. the share of completed projects. */
@Composable
fun PpSemiCircleGauge(
    progress: Float,
    value: String,
    caption: String,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    color: Color = PortProofTheme.colors.accent,
) {
    val track = PortProofTheme.colors.surfaceSunken
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) { animated.animateTo(progress.coerceIn(0f, 1f), tween(durationMillis = 700)) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .clearAndSetSemantics { contentDescription = accessibilityLabel },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(2f)) {
            val strokeWidth = size.height * 0.16f
            val diameter = size.width - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            drawArc(track, 180f, 180f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
            if (animated.value > 0f) {
                drawArc(color, 180f, 180f * animated.value, false, topLeft = topLeft, size = arcSize, style = stroke)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 4.dp)) {
            Text(caption, style = MaterialTheme.typography.labelMedium, color = PortProofTheme.colors.textSecondary)
            Text(value, style = PortProofTheme.numbers.metricLarge, color = PortProofTheme.colors.textPrimary)
        }
    }
}
