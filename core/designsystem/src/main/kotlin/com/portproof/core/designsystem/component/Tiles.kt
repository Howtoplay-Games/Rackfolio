package com.portproof.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme

/** Tall glass action tile: icon circle (top left), count badge (top right), two-line label (bottom left). */
@Composable
fun PpGlassTile(
    icon: ImageVector,
    label: String,
    badge: String?,
    accessibilityLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PortProofTheme.colors
    PpGlassCard(
        modifier = modifier
            .heightIn(min = 152.dp)
            .clearAndSetSemantics {
                contentDescription = accessibilityLabel
                role = Role.Button
                onClick { onClick(); true }
            },
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(38.dp)
                    .background(colors.surface, PortProofTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = colors.textPrimary, modifier = Modifier.size(17.dp)) }
            Spacer(Modifier.weight(1f))
            if (badge != null) {
                Text(
                    badge,
                    style = PortProofTheme.numbers.caption,
                    color = colors.textSecondary,
                    modifier = Modifier
                        .background(colors.glassBorder.copy(alpha = 0.7f), PortProofTheme.shapes.pill)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(44.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary, minLines = 2, maxLines = 2)
    }
}

/** Glass search pill that opens the search screen ("Search a document"). */
@Composable
fun PpSearchPill(placeholder: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = PortProofTheme.shapes.pill,
        color = colors.glass,
        border = BorderStroke(1.dp, colors.glassBorder),
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(PortProofIcons.Search, null, tint = colors.textPrimary, modifier = Modifier.size(17.dp))
            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, maxLines = 1)
        }
    }
}

/** Big-number card for overview screens. [filled] renders the accent (periwinkle) variant. */
@Composable
fun PpStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    filled: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    val content = if (filled) colors.onAccent else colors.textPrimary
    val secondary = if (filled) colors.onAccent.copy(alpha = 0.85f) else colors.textSecondary
    val body: @Composable () -> Unit = {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = secondary, minLines = 2, maxLines = 2)
            Spacer(Modifier.height(14.dp))
            Text(value, style = PortProofTheme.numbers.metricLarge, color = content)
            if (caption != null) Text(caption, style = MaterialTheme.typography.labelSmall, color = secondary, maxLines = 1)
        }
    }
    val shape = PortProofTheme.shapes.card
    val container = if (filled) colors.accentStrong else colors.surface
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = container) { body() }
    } else {
        Surface(modifier = modifier, shape = shape, color = container) { body() }
    }
}
