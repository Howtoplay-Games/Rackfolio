package com.portproof.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.R
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme

/**
 * Header row from the reference dashboard: a square button on each side and a small centred title.
 * Pass [onBack] for pushed screens; [leading] replaces the back button on top-level screens.
 */
@Composable
fun PpTopBar(
    title: String?,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .heightIn(min = 64.dp)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = PortProofTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 64.dp)
                    .semantics { heading() },
            )
        }
        Row(Modifier.fillMaxWidth().horizontalBleed(2.dp), verticalAlignment = Alignment.CenterVertically) {
            when {
                leading != null -> leading()
                onBack != null -> PpIconButton(PortProofIcons.ChevronLeft, stringResource(R.string.designsystem_back), onBack)
            }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }
}

/** Dark bottom bar with rounded top corners. Fill it with [PpBarItem]s and one [PpBarCenterItem]. */
@Composable
fun PpBottomBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val colors = PortProofTheme.colors
    Surface(modifier = modifier.fillMaxWidth(), shape = PortProofTheme.shapes.bar, color = colors.navBar) {
        Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars), contentAlignment = Alignment.Center) {
            Row(
                Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/** Icon-only destination, as in the reference; [label] is announced by screen readers. */
@Composable
fun RowScope.PpBarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = PortProofTheme.colors
    Box(
        Modifier
            .weight(1f)
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = ripple(bounded = false, radius = 26.dp, color = colors.onNavBar),
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) colors.accent else colors.onNavBarMuted, modifier = Modifier.size(22.dp))
    }
}

/** The orange square in the middle of the bar. */
@Composable
fun RowScope.PpBarCenterItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = PortProofTheme.colors
    Box(
        Modifier
            .weight(1f)
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = ripple(bounded = false, radius = 30.dp, color = colors.onNavBar),
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Surface(shape = PortProofTheme.shapes.control, color = colors.accent, modifier = Modifier.size(50.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(24.dp))
            }
        }
    }
}
