package com.portproof.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Row of rounded segments, the first [filled] in the accent colour (reference review progress). */
@Composable
fun PpSegmentedProgress(segments: Int, filled: Int, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    Canvas(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(filled.toFloat(), 0f..segments.toFloat(), segments) },
    ) {
        val gap = 5.dp.toPx()
        val width = (size.width - gap * (segments - 1)) / segments
        repeat(segments) { i ->
            drawRoundRect(
                color = if (i < filled) colors.accent else colors.accentTrack,
                topLeft = Offset(i * (width + gap), 0f),
                size = Size(width, size.height),
                cornerRadius = CornerRadius(size.height / 2),
            )
        }
    }
}

/** White status card under a detail title: orange icon square, title, caption, segmented progress. */
@Composable
fun PpReviewCard(
    icon: ImageVector,
    title: String,
    caption: String,
    segments: Int,
    filled: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    val body: @Composable () -> Unit = {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).background(colors.accent, PortProofTheme.shapes.control), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = colors.onAccent, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 1)
                Text(caption, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 2)
                Spacer(Modifier.height(10.dp))
                PpSegmentedProgress(segments, filled)
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = PortProofTheme.shapes.card, color = colors.surface) { body() }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = PortProofTheme.shapes.card, color = colors.surface) { body() }
    }
}

enum class PpHighlight { None, Red, Orange, Yellow }

/** Band and text colours for a highlight (unspecified band and secondary text for [PpHighlight.None]). */
@Composable
fun highlightColors(highlight: PpHighlight): Pair<Color, Color> {
    val colors = PortProofTheme.colors
    return when (highlight) {
        PpHighlight.None -> Color.Unspecified to colors.textSecondary
        PpHighlight.Red -> colors.highlightRed to colors.onHighlightRed
        PpHighlight.Orange -> colors.highlightOrange to colors.onHighlightOrange
        PpHighlight.Yellow -> colors.highlightYellow to colors.onHighlightYellow
    }
}

/** A numbered section of a [PpDocumentCard]; [highlight] marks text that needs attention. */
data class PpDocSection(val title: String, val body: String, val highlight: PpHighlight = PpHighlight.None)

/**
 * Document-style card (reference "Master Services Agreement.pdf"): file header, centred uppercase
 * heading, numbered sections whose body text can carry a coloured highlight band.
 */
@Composable
fun PpDocumentCard(
    icon: ImageVector,
    title: String,
    meta: String,
    heading: String,
    sections: List<PpDocSection>,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    Surface(modifier = modifier.fillMaxWidth(), shape = PortProofTheme.shapes.card, color = colors.surface) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics(mergeDescendants = true) {}) {
                Icon(icon, null, tint = colors.textPrimary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 2)
                    Text(meta, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary, maxLines = 1)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                heading.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                sections.forEachIndexed { index, section -> DocSection(index + 1, section) }
            }
            if (footer != null) {
                Spacer(Modifier.height(14.dp))
                footer()
            }
        }
    }
}

@Composable
private fun DocSection(number: Int, section: PpDocSection) {
    val colors = PortProofTheme.colors
    val (band, ink) = highlightColors(section.highlight)
    Column(Modifier.semantics(mergeDescendants = true) {}) {
        Text("$number.  ${section.title}", style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            text = buildAnnotatedString { withStyle(SpanStyle(background = band, color = ink)) { append(section.body) } },
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.55f),
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}
