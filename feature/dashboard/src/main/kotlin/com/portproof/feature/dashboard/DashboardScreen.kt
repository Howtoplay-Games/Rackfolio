package com.portproof.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpIconButtonShape
import com.portproof.core.designsystem.component.horizontalBleed
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.ui.LocalBottomBarInset
import com.portproof.core.ui.currentLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal data class DashboardActions(
    val onSearch: () -> Unit,
    val onCreateProject: () -> Unit,
    val onOpenProject: (ProjectId) -> Unit,
    val onOpenProjects: (ProjectListFilter) -> Unit,
    val onOpenSettings: () -> Unit,
)

@Composable
internal fun DashboardRoute(
    onSearch: () -> Unit,
    onCreateProject: () -> Unit,
    onOpenProject: (ProjectId) -> Unit,
    onOpenProjects: (ProjectListFilter) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(state, DashboardActions(onSearch, onCreateProject, onOpenProject, onOpenProjects, onOpenSettings))
}

/** Two columns from 840dp (tablets, unfolded foldables); a single column below. */
@Composable
internal fun DashboardScreen(state: DashboardUiState, actions: DashboardActions) {
    val spacing = PortProofTheme.spacing
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val twoColumns = maxWidth >= 840.dp
        val scrollableStats = maxWidth < 600.dp
        Column(
            modifier = Modifier
                .widthIn(max = if (twoColumns) 1200.dp else spacing.maxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = spacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
        ) {
            DashboardHeader(state, actions)
            if (state.isLoading) return@Column
            if (state.summary.isEmpty) {
                WelcomeCard(actions.onCreateProject)
                QuickActionsCard(actions)
            } else if (twoColumns) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
                    Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                        OverviewSection(state, actions, scrollable = false)
                        StatusCard(state.summary) { actions.onOpenProjects(ProjectListFilter.ALL) }
                        state.trend?.let { ActivityTrendCard(it) }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                        QuickActionsCard(actions)
                        RecentSections(state, actions)
                    }
                }
            } else {
                OverviewSection(state, actions, scrollable = scrollableStats)
                QuickActionsCard(actions)
                StatusCard(state.summary) { actions.onOpenProjects(ProjectListFilter.ALL) }
                state.trend?.let { ActivityTrendCard(it) }
                RecentSections(state, actions)
            }
            Spacer(Modifier.height(LocalBottomBarInset.current))
        }
    }
}

@Composable
private fun DashboardHeader(state: DashboardUiState, actions: DashboardActions) {
    val colors = PortProofTheme.colors
    val today = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(currentLocale()).format(LocalDate.now())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 48dp touch targets draw 40dp visuals; bleeding 4dp aligns them with the card edges.
            .horizontalBleed(4.dp)
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PpIconButton(PortProofIcons.Search, stringResource(R.string.dashboard_search), actions.onSearch, modifier = Modifier.padding(end = 4.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (state.firstName.isEmpty()) {
                    stringResource(R.string.dashboard_hello)
                } else {
                    stringResource(R.string.dashboard_hello_name, state.firstName)
                },
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Text(today, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 1)
        }
        PpIconButton(
            icon = PortProofIcons.Add,
            contentDescription = stringResource(R.string.dashboard_action_new),
            onClick = actions.onCreateProject,
            shape = PpIconButtonShape.Circle,
            filled = true,
        )
        PpAvatarButton(
            name = state.technicianName,
            contentDescription = stringResource(R.string.dashboard_profile),
            onClick = actions.onOpenSettings,
        )
    }
}

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = PortProofTheme.colors.textPrimary,
        modifier = modifier.semantics { heading() },
    )
}

internal typealias CardContent = @Composable ColumnScope.() -> Unit
