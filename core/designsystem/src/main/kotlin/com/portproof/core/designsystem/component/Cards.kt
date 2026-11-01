package com.portproof.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme
import kotlin.math.cos
import kotlin.math.sin

/** Dark hero card with a progress ring, e.g. "Project Progress · 8 projects left · 60%". */
@Composable
fun PpHeroProgressCard(
    title: String,
    subtitle: String,
    progress: Float,
    progressLabel: String,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .clearAndSetSemantics { contentDescription = accessibilityLabel }
                .padding(start = 22.dp, end = 16.dp, top = 13.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onHero)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onHeroMuted)
            }
            PpProgressRing(progress = progress, label = progressLabel)
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = PortProofTheme.shapes.hero, color = colors.hero) { content() }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = PortProofTheme.shapes.hero, color = colors.hero) { content() }
    }
}

/** White arc drawn anticlockwise from 12 o'clock (no track, as in the reference) ending in a violet knob. */
@Composable
fun PpProgressRing(progress: Float, label: String, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) { animated.animateTo(progress.coerceIn(0f, 1f), tween(durationMillis = 800)) }
    Box(modifier.size(68.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(68.dp)) {
            val stroke = 6.dp.toPx()
            val knob = 7.dp.toPx()
            val inset = knob
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            val sweep = -360f * animated.value
            if (animated.value > 0f) {
                drawArc(colors.onHero, -90f, sweep, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            val radius = arcSize.width / 2
            val angle = Math.toRadians((-90f + sweep).toDouble())
            val end = Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
            drawCircle(colors.onHero, radius = knob, center = end)
            drawCircle(colors.accent, radius = knob - 2.dp.toPx(), center = end)
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onHero)
    }
}

/** Metric tile: two-line label, emoji badge, large figure with a grey unit ("1.4k kCal" style). */
@Composable
fun PpMetricCard(
    label: String,
    emoji: PpEmoji,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    PpCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(14.dp)) {
        Column(Modifier.semantics(mergeDescendants = true) {}) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(top = 2.dp),
                )
                PpEmojiBadge(emoji)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(value, style = PortProofTheme.numbers.metric, color = colors.textPrimary)
                Text(
                    unit,
                    style = PortProofTheme.numbers.metricUnit,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
        }
    }
}

/** Card header with an emoji badge, title and caption ("Heart Rate · Last 7 days"), then content. */
@Composable
fun PpInsightCard(
    emoji: PpEmoji,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    PpCard(modifier = modifier, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics(mergeDescendants = true) {}) {
            PpEmojiBadge(emoji, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = PortProofTheme.colors.textPrimary)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = PortProofTheme.colors.textSecondary)
            }
        }
        Spacer(Modifier.height(14.dp))
        content()
    }
}

/**
 * Pastel feature card ("Squat Low, Build Strength" in the reference): title, caption, a black pill
 * button and artwork on the right. The whole card and the pill both trigger [onClick].
 */
@Composable
fun PpFeatureCard(
    title: String,
    caption: String,
    buttonLabel: String,
    illustration: PpIllustration,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PortProofTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 176.dp),
        shape = PortProofTheme.shapes.feature,
        color = containerColor,
    ) {
        Box {
            Image(
                painter = painterResource(illustration.res),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 14.dp)
                    .size(124.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .padding(start = 22.dp, top = 22.dp, bottom = 12.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onPastel,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPastelMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                PpPillButton(buttonLabel, onClick)
            }
        }
    }
}
