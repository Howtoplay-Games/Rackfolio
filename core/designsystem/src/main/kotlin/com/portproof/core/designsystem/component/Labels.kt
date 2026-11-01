package com.portproof.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Section title with an optional trailing action, e.g. "Recent projects  [View all]". */
@Composable
fun PpSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    action: (@Composable () -> Unit)? = null,
) {
    // With an action the row reserves a 48dp touch target; without one it stays compact.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (action != null) Modifier.heightIn(min = 48.dp) else Modifier.padding(bottom = 10.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = style,
            color = PortProofTheme.colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        action?.invoke()
    }
}

/** Compact status label with a coloured dot. Colour is never the only signal: text is always shown. */
@Composable
fun PpStatusPill(text: String, tone: PpTone, modifier: Modifier = Modifier) {
    val toneColors = tone.colors()
    Row(
        modifier = modifier
            .background(toneColors.container, PortProofTheme.shapes.pill)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(toneColors.indicator, PortProofTheme.shapes.pill),
        )
        Text(text, style = MaterialTheme.typography.labelSmall, color = toneColors.content, maxLines = 1)
    }
}

/** Small caption + value pair, e.g. "Start date / 12 Mar 2026". */
@Composable
fun PpLabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(modifier.semantics(mergeDescendants = true) {}) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = PortProofTheme.colors.textMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = PortProofTheme.colors.textPrimary)
    }
}
