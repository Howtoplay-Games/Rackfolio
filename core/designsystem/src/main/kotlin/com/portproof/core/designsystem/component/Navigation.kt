package com.portproof.core.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

data class PpNavItem(val key: String, val label: String, val icon: ImageVector)

/** Height of [PpFloatingNavBar] including its bottom margin, for content padding. */
val PpFloatingNavBarReservedHeight = 64.dp + 16.dp + 12.dp

/**
 * Floating pill navigation bar. Unselected destinations are icon circles; the selected one expands
 * into a pill with an inverse (near-black) icon circle and its label.
 */
@Composable
fun PpFloatingNavBar(
    items: List<PpNavItem>,
    selectedKey: String?,
    onSelect: (PpNavItem) -> Unit,
    modifier: Modifier = Modifier,
    navigationLabel: String,
) {
    val colors = PortProofTheme.colors
    Surface(
        modifier = modifier
            .shadow(16.dp, PortProofTheme.shapes.pill, ambientColor = Color.Black.copy(0.10f), spotColor = Color.Black.copy(0.14f))
            .semantics { contentDescription = navigationLabel },
        shape = PortProofTheme.shapes.pill,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 8.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.key == selectedKey
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .background(colors.surfaceMuted, PortProofTheme.shapes.pill)
                        .selectable(selected = selected, role = Role.Tab) { onSelect(item) }
                        .semantics(mergeDescendants = true) { contentDescription = item.label }
                        .animateContentSize(spring(stiffness = 600f)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavIconCircle(item.icon, selected)
                    if (selected) {
                        Spacer(Modifier.width(10.dp))
                        Text(item.label, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(4.dp).background(colors.textPrimary, PortProofTheme.shapes.pill))
                        Spacer(Modifier.width(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIconCircle(icon: ImageVector, selected: Boolean) {
    val colors = PortProofTheme.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(if (selected) colors.inverseSurface else Color.Transparent, PortProofTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) colors.onInverseSurface else colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Navigation rail for medium and expanded widths, using the same visual language. */
@Composable
fun PpNavigationRail(
    items: List<PpNavItem>,
    selectedKey: String?,
    onSelect: (PpNavItem) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(88.dp)
            .background(colors.surface)
            .padding(vertical = 16.dp)
            .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        header?.invoke()
        items.forEach { item ->
            val selected = item.key == selectedKey
            Column(
                modifier = Modifier
                    .selectable(selected = selected, role = Role.Tab) { onSelect(item) }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.background(colors.surfaceMuted, PortProofTheme.shapes.pill)) { NavIconCircle(item.icon, selected) }
                Spacer(Modifier.height(4.dp))
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) colors.textPrimary else colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
