package com.portproof.feature.insights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpListScreen
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.ui.ActivityRow
import com.portproof.core.ui.formatDate
import java.time.LocalDate
import java.time.ZoneId

@Composable
internal fun ActivityLogRoute(onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit, viewModel: ActivityLogViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityLogScreen(state, onBack, onOpenProject)
}

/** Activity grouped by local day ("Today", "Yesterday", then dates), one card per day. */
@Composable
internal fun ActivityLogScreen(state: ActivityLogUiState, onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit) {
    val spacing = PortProofTheme.spacing
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val groups = state.entries.groupBy { it.occurredAt.atZone(zone).toLocalDate() }
    PpListScreen {
        Column(Modifier.padding(horizontal = spacing.screenGutter)) {
            PpTopBar(stringResource(R.string.activity_title), onBack = onBack)
        }
        if (!state.isLoading && state.entries.isEmpty()) {
            PpEmptyState(
                icon = PortProofIcons.Activity,
                title = stringResource(R.string.activity_empty_title),
                message = stringResource(R.string.activity_empty_message),
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start = spacing.screenGutter,
                end = spacing.screenGutter,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + spacing.lg,
            ),
        ) {
            groups.forEach { (date, entries) ->
                item(key = "header-$date") {
                    Text(
                        text = when (date) {
                            today -> stringResource(R.string.activity_today)
                            today.minusDays(1) -> stringResource(R.string.activity_yesterday)
                            else -> formatDate(date)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = PortProofTheme.colors.textPrimary,
                        modifier = Modifier
                            .padding(top = spacing.lg, bottom = spacing.sm)
                            .semantics { heading() },
                    )
                }
                item(key = "day-$date") {
                    PpCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
                        entries.forEach { entry ->
                            val projectId = entry.projectId
                            ActivityRow(entry, onClick = projectId?.let { id -> { onOpenProject(id) } })
                        }
                    }
                }
            }
        }
    }
}
