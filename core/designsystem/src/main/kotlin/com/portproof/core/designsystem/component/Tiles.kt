package com.portproof.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/**
 * Category tile from the reference's recipe screen: a rounded square with an icon and a label
 * underneath. The selected tile is dark.
 */
@Composable
fun PpCategoryTile(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PortProofTheme.colors
    Column(
        modifier
            .widthIn(min = 72.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(62.dp)
                .background(if (selected) colors.inverseSurface else colors.surface, PortProofTheme.shapes.tile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = if (selected) colors.onInverseSurface else colors.textPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium),
            color = if (selected) colors.textPrimary else colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/** Small orange count badge ("Trending Recipes 8"). */
@Composable
fun PpCountBadge(text: String, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    Box(
        modifier
            .widthIn(min = 20.dp)
            .background(colors.accent, PortProofTheme.shapes.pill)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = PortProofTheme.numbers.caption, color = colors.onAccent)
    }
}

/** Big-number card for overview screens. [filled] renders the orange variant. */
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
    val secondary = if (filled) colors.onAccent else colors.textSecondary
    val body: @Composable () -> Unit = {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = secondary, minLines = 2, maxLines = 2)
            Spacer(Modifier.height(10.dp))
            Text(value, style = PortProofTheme.numbers.metricLarge, color = content)
            if (caption != null) Text(caption, style = MaterialTheme.typography.labelSmall, color = secondary, maxLines = 1)
        }
    }
    val shape = PortProofTheme.shapes.card
    val container = if (filled) colors.accent else colors.surface
    val border = if (!filled && colors.isDark) BorderStroke(1.dp, colors.border) else null
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = container, border = border) { body() }
    } else {
        Surface(modifier = modifier, shape = shape, color = container, border = border) { body() }
    }
}
