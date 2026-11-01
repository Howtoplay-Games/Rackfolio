package com.portproof.core.designsystem.component

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/** Space to leave under scrolling content for [PpFloatingNavBar]: bar height plus its margin. */
val PpFloatingNavBarReservedHeight = 58.dp + 16.dp + 12.dp

/** Dark floating pill with icon-only destinations; the selected one sits in a violet circle. */
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
            .shadow(18.dp, PortProofTheme.shapes.pill, ambientColor = Color.Black.copy(0.18f), spotColor = Color.Black.copy(0.25f))
            .semantics { contentDescription = navigationLabel },
        shape = PortProofTheme.shapes.pill,
        color = colors.navBar,
    ) {
        Row(
            modifier = Modifier
                .height(58.dp)
                .padding(horizontal = 6.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item -> NavCircle(item, item.key == selectedKey, onSelect) }
        }
    }
}

@Composable
private fun NavCircle(item: PpNavItem, selected: Boolean, onSelect: (PpNavItem) -> Unit) {
    val colors = PortProofTheme.colors
    val background by animateColorAsState(if (selected) colors.accent else Color.Transparent, label = "navItem")
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(PortProofTheme.shapes.pill) // keeps the press ripple circular
            .selectable(selected = selected, role = Role.Tab) { onSelect(item) }
            .semantics { contentDescription = item.label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(background, PortProofTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = if (selected) colors.onAccent else colors.navIcon,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

/** Navigation rail for medium and expanded widths, in the same dark pill language. */
@Composable
fun PpNavigationRail(
    items: List<PpNavItem>,
    selectedKey: String?,
    onSelect: (PpNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PortProofTheme.colors
    Box(modifier.fillMaxHeight().padding(12.dp), contentAlignment = Alignment.Center) {
        Surface(shape = PortProofTheme.shapes.card, color = colors.navBar) {
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .padding(vertical = 14.dp)
                    .selectableGroup(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items.forEach { item ->
                    val selected = item.key == selectedKey
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        NavCircle(item, selected, onSelect)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) colors.navIcon else colors.navIcon.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
