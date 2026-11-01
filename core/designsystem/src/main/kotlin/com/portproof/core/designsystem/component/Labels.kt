package com.portproof.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
    badge: String? = null,
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
            modifier = Modifier.semantics { heading() },
        )
        if (badge != null) PpCountBadge(badge, Modifier.padding(start = 8.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        action?.invoke()
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
