package com.portproof.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpAvatarButton
import com.portproof.core.designsystem.component.PpBracketLogo
import com.portproof.core.designsystem.component.PpCanvas
import com.portproof.core.designsystem.component.PpCircleButton
import com.portproof.core.designsystem.component.PpCircleStyle
import com.portproof.core.designsystem.component.PpGlassTile
import com.portproof.core.designsystem.component.PpSearchPill
import com.portproof.core.designsystem.component.horizontalBleed
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.ui.currentLocale
import com.portproof.core.ui.greetingRes
import com.portproof.core.ui.shortDateLine

internal data class DashboardActions(
    val onOpenOverview: () -> Unit,
    val onCreateProject: () -> Unit,
    val onOpenProjects: (ProjectListFilter) -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenSearch: () -> Unit,
)

@Composable
internal fun DashboardRoute(actions: DashboardActions, viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(state, actions)
}

/** Home, laid out as the reference's first screen. Content hugs the bottom; the glow fills the top. */
@Composable
internal fun DashboardScreen(state: DashboardUiState, actions: DashboardActions) {
    val spacing = PortProofTheme.spacing
    val summary = state.summary
    PpCanvas(Modifier.fillMaxSize(), glow = true) {
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = spacing.screenGutter),
        ) {
            Row(Modifier.fillMaxWidth().horizontalBleed(4.dp).padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                PpBracketLogo(Modifier.padding(start = 4.dp), size = 44.dp, contentDescription = stringResource(R.string.dashboard_logo))
                Spacer(Modifier.weight(1f))
                PpAvatarButton(state.technicianName, stringResource(R.string.dashboard_profile), actions.onOpenSettings)
                PpCircleButton(PortProofIcons.Grid, stringResource(R.string.dashboard_overview), actions.onOpenOverview, style = PpCircleStyle.Dark)
            }
            Spacer(Modifier.weight(1f))
            Greeting(state)
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                PpGlassTile(
                    icon = PortProofIcons.Activity,
                    label = stringResource(R.string.dashboard_tile_active),
                    badge = summary.active.toString(),
                    accessibilityLabel = stringResource(R.string.dashboard_tile_active_a11y, summary.active),
                    onClick = { actions.onOpenProjects(ProjectListFilter.ACTIVE) },
                    modifier = Modifier.weight(1f),
                )
                PpGlassTile(
                    icon = PortProofIcons.CircleCheck,
                    label = stringResource(R.string.dashboard_tile_completed),
                    badge = "${summary.completed}/${summary.total}",
                    accessibilityLabel = stringResource(R.string.dashboard_tile_completed_a11y, summary.completed, summary.total),
                    onClick = { actions.onOpenProjects(ProjectListFilter.COMPLETED) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.horizontalBleed(2.dp)) {
                PpSearchPill(stringResource(R.string.dashboard_search), actions.onOpenSearch, Modifier.weight(1f).padding(start = 2.dp))
                PpCircleButton(PortProofIcons.Add, stringResource(R.string.dashboard_new_project), actions.onCreateProject, visualSize = 52.dp)
            }
            Spacer(Modifier.height(spacing.md))
        }
    }
}

@Composable
private fun Greeting(state: DashboardUiState) {
    val colors = PortProofTheme.colors
    val greeting = stringResource(greetingRes())
    Text(shortDateLine(state.today, currentLocale()), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    Spacer(Modifier.height(6.dp))
    Text(
        text = if (state.isLoading || state.firstName.isEmpty()) greeting else stringResource(R.string.dashboard_greeting_name, greeting, state.firstName),
        style = MaterialTheme.typography.headlineMedium,
        color = colors.textPrimary,
        modifier = Modifier.semantics { heading() },
    )
}
