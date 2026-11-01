package com.portproof.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme

enum class PpTrend { Up, Down, Flat }

/** Small trend line under a metric: coloured arrow badge, emphasised value and a caption. */
@Composable
fun PpDelta(value: String, caption: String, trend: PpTrend, tone: PpTone, modifier: Modifier = Modifier) {
    val toneColors = tone.colors()
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(toneColors.indicator, PortProofTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            val icon = when (trend) {
                PpTrend.Up -> PortProofIcons.ArrowUp
                PpTrend.Down -> PortProofIcons.ArrowDown
                PpTrend.Flat -> PortProofIcons.ArrowRight
            }
            Icon(icon, null, tint = PortProofTheme.colors.surface, modifier = Modifier.size(11.dp))
        }
        Text(value, style = PortProofTheme.numbers.caption, color = toneColors.content)
        Text(
            caption,
            style = MaterialTheme.typography.labelSmall,
            color = PortProofTheme.colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Metric card: icon tile + label, large figure, optional trend line. */
@Composable
fun PpStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    delta: (@Composable () -> Unit)? = null,
) {
    PpCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PpIconTile(icon = icon, size = 28.dp, iconSize = 15.dp)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = PortProofTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(value, style = PortProofTheme.numbers.metric, color = PortProofTheme.colors.textPrimary)
        if (delta != null) {
            Spacer(Modifier.height(8.dp))
            delta()
        }
    }
}
