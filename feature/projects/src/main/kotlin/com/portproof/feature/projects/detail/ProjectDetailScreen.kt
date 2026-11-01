package com.portproof.feature.projects.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpCircleButton
import com.portproof.core.designsystem.component.PpCircleStyle
import com.portproof.core.designsystem.component.PpConfirmDialog
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpGradientScreen
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.designsystem.component.PpReviewCard
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.core.ui.ActivityRow
import com.portproof.core.ui.icon
import com.portproof.core.ui.labelRes
import com.portproof.feature.projects.R
import com.portproof.core.ui.R as UiR

@Composable
internal fun ProjectDetailRoute(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onEdit: (ProjectId) -> Unit,
    onDeleted: () -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(deleted) { if (deleted) onDeleted() }

    val archivedText = stringResource(R.string.project_archived_message)
    val restoredText = stringResource(R.string.project_restored_message)
    val undoText = stringResource(UiR.string.ui_action_undo)
    val errorText = stringResource(UiR.string.ui_error_generic)
    LaunchedEffect(message) {
        val current = message ?: return@LaunchedEffect
        viewModel.onMessageShown()
        when (current) {
            ProjectDetailMessage.Archived -> {
                val result = snackbarHostState.showSnackbar(archivedText, undoText, duration = SnackbarDuration.Short)
                if (result == SnackbarResult.ActionPerformed) viewModel.restore()
            }
            ProjectDetailMessage.Restored -> snackbarHostState.showSnackbar(restoredText)
            ProjectDetailMessage.Error -> snackbarHostState.showSnackbar(errorText)
        }
    }

    ProjectDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        actions = DetailActions(
            onBack = onBack,
            onHome = onHome,
            onEdit = { onEdit(viewModel.projectId) },
            onStatusSelected = viewModel::setStatus,
            onArchive = viewModel::archive,
            onRestore = viewModel::restore,
            onDelete = viewModel::delete,
        ),
    )
}

internal data class DetailActions(
    val onBack: () -> Unit,
    val onHome: () -> Unit,
    val onEdit: () -> Unit,
    val onStatusSelected: (ProjectStatus) -> Unit,
    val onArchive: () -> Unit,
    val onRestore: () -> Unit,
    val onDelete: () -> Unit,
)

@Composable
internal fun ProjectDetailScreen(state: ProjectDetailUiState, snackbarHostState: SnackbarHostState, actions: DetailActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val spacing = PortProofTheme.spacing
    val success = state as? ProjectDetailUiState.Success
    Box {
        PpGradientScreen(
            title = success?.project?.name ?: stringResource(R.string.project_detail_title),
            onBack = actions.onBack,
            onClose = actions.onHome,
            headerCard = success?.let { { StatusCard(it, actions.onStatusSelected) } },
            bottomBar = success?.let {
                {
                    PpPrimaryButton(stringResource(R.string.project_edit), actions.onEdit, Modifier.weight(1f))
                    MoreMenu(it.project.isArchived, actions.onArchive, actions.onRestore, onDelete = { confirmDelete = true })
                }
            },
        ) {
            when (state) {
                ProjectDetailUiState.Loading -> Unit
                ProjectDetailUiState.NotFound -> PpEmptyState(
                    icon = PortProofIcons.Alert,
                    title = stringResource(R.string.project_not_found_title),
                    message = stringResource(R.string.project_not_found_message),
                )
                is ProjectDetailUiState.Success -> Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = spacing.screenGutter, vertical = spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    ProjectRecord(state)
                    ActivityCard(state)
                }
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp))
    }
    if (confirmDelete && success != null) {
        PpConfirmDialog(
            title = stringResource(R.string.project_delete_title),
            message = stringResource(R.string.project_delete_message, success.project.name),
            confirmLabel = stringResource(R.string.project_delete_confirm),
            dismissLabel = stringResource(UiR.string.ui_action_cancel),
            onConfirm = {
                confirmDelete = false
                actions.onDelete()
            },
            onDismiss = { confirmDelete = false },
            destructive = true,
        )
    }
}

/** Status card on the header: tap to change status; the bar shows schedule (or status) progress. */
@Composable
private fun StatusCard(state: ProjectDetailUiState.Success, onStatusSelected: (ProjectStatus) -> Unit) {
    val project = state.project
    var expanded by remember { mutableStateOf(false) }
    val scheduleLabel = when (val s = state.schedule) {
        is ScheduleState.Upcoming -> pluralStringResource(R.plurals.project_schedule_not_started, s.daysUntilStart.toInt(), s.daysUntilStart)
        is ScheduleState.Running -> stringResource(R.string.project_schedule_progress, s.day, s.totalDays)
        is ScheduleState.Overdue -> pluralStringResource(R.plurals.project_schedule_overdue, s.daysOver.toInt(), s.daysOver)
        ScheduleState.Done, null -> null
    }
    val issues = state.review.issueCount
    Box {
        PpReviewCard(
            icon = project.status.icon,
            title = listOfNotNull(stringResource(project.status.labelRes), scheduleLabel).joinToString(" · "),
            caption = if (issues == 0) {
                stringResource(R.string.project_review_complete)
            } else {
                pluralStringResource(R.plurals.project_review_issues, issues, issues)
            },
            segments = 5,
            filled = progressSegments(project, state.schedule),
            onClick = if (project.isArchived) null else ({ expanded = true }),
            trailing = if (project.isArchived) {
                null
            } else {
                { Icon(PortProofIcons.ChevronDown, stringResource(R.string.project_change_status), tint = PortProofTheme.colors.textSecondary) }
            },
        )
        DropdownMenu(expanded, onDismissRequest = { expanded = false }, containerColor = PortProofTheme.colors.surface) {
            ProjectStatus.entries.forEach { status ->
                MenuItem(stringResource(status.labelRes), status.icon, checked = status == project.status) {
                    expanded = false
                    onStatusSelected(status)
                }
            }
        }
    }
}

@Composable
private fun MoreMenu(isArchived: Boolean, onArchive: () -> Unit, onRestore: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PpCircleButton(PortProofIcons.Ellipsis, stringResource(UiR.string.ui_action_more), { expanded = true }, style = PpCircleStyle.Accent, visualSize = 56.dp)
        DropdownMenu(expanded, onDismissRequest = { expanded = false }, containerColor = PortProofTheme.colors.surface) {
            MenuItem(
                text = stringResource(if (isArchived) UiR.string.ui_action_restore else UiR.string.ui_action_archive),
                icon = if (isArchived) PortProofIcons.Restore else PortProofIcons.Archive,
            ) {
                expanded = false
                if (isArchived) onRestore() else onArchive()
            }
            MenuItem(stringResource(UiR.string.ui_action_delete), PortProofIcons.Delete, destructive = true) {
                expanded = false
                onDelete()
            }
        }
    }
}

@Composable
private fun MenuItem(text: String, icon: ImageVector, destructive: Boolean = false, checked: Boolean = false, onClick: () -> Unit) {
    val color = if (destructive) PortProofTheme.colors.dangerText else PortProofTheme.colors.textPrimary
    DropdownMenuItem(
        text = { Text(text, style = MaterialTheme.typography.bodyMedium, color = color) },
        leadingIcon = { Icon(icon, null, tint = color, modifier = Modifier.size(18.dp)) },
        trailingIcon = if (checked) ({ Icon(PortProofIcons.Check, null, tint = PortProofTheme.colors.accentStrong) }) else null,
        onClick = onClick,
    )
}

@Composable
private fun ActivityCard(state: ProjectDetailUiState.Success) {
    PpCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        PpSectionHeader(stringResource(R.string.project_section_activity), style = MaterialTheme.typography.titleSmall)
        if (state.activity.isEmpty()) {
            Text(stringResource(R.string.project_no_activity), style = MaterialTheme.typography.bodySmall, color = PortProofTheme.colors.textSecondary)
            Spacer(Modifier.height(8.dp))
        }
        state.activity.forEach { entry -> ActivityRow(entry, showProject = false) }
    }
}
