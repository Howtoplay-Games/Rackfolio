package com.portproof.feature.projects.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.portproof.core.designsystem.component.PpConfirmDialog
import com.portproof.core.designsystem.component.PpDivider
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpHatchedProgressBar
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpIconButtonShape
import com.portproof.core.designsystem.component.PpLabeledValue
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.component.PpSegmentedControl
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.core.ui.ActivityRow
import com.portproof.core.ui.ProjectStatusPill
import com.portproof.core.ui.formatDate
import com.portproof.core.ui.labelRes
import com.portproof.feature.projects.R
import com.portproof.core.designsystem.R as DsR
import com.portproof.core.ui.R as UiR

@Composable
internal fun ProjectDetailRoute(
    onBack: () -> Unit,
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
        onBack = onBack,
        onEdit = { onEdit(viewModel.projectId) },
        onStatusSelected = viewModel::setStatus,
        onArchive = viewModel::archive,
        onRestore = viewModel::restore,
        onDelete = viewModel::delete,
    )
}

@Composable
internal fun ProjectDetailScreen(
    state: ProjectDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onStatusSelected: (ProjectStatus) -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val spacing = PortProofTheme.spacing
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            PpTopBar(
                title = stringResource(R.string.project_detail_title),
                modifier = Modifier.widthIn(max = spacing.maxContentWidth),
                navigationIcon = {
                    PpIconButton(PortProofIcons.Back, stringResource(DsR.string.designsystem_back), onBack, shape = PpIconButtonShape.Circle)
                },
                actions = {
                    if (state is ProjectDetailUiState.Success) {
                        PpIconButton(PortProofIcons.Edit, stringResource(UiR.string.ui_action_edit), onEdit)
                        OverflowMenu(state.project.isArchived, onArchive, onRestore, onDelete = { confirmDelete = true })
                    }
                },
            )
            when (state) {
                ProjectDetailUiState.Loading -> Unit
                ProjectDetailUiState.NotFound -> PpEmptyState(
                    icon = PortProofIcons.Alert,
                    title = stringResource(R.string.project_not_found_title),
                    message = stringResource(R.string.project_not_found_message),
                )
                is ProjectDetailUiState.Success -> DetailContent(state, onStatusSelected, onRestore)
            }
        }
        SnackbarHost(
            snackbarHostState,
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars),
        )
    }
    if (confirmDelete && state is ProjectDetailUiState.Success) {
        PpConfirmDialog(
            title = stringResource(R.string.project_delete_title),
            message = stringResource(R.string.project_delete_message, state.project.name),
            confirmLabel = stringResource(R.string.project_delete_confirm),
            dismissLabel = stringResource(UiR.string.ui_action_cancel),
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
            destructive = true,
        )
    }
}

@Composable
private fun OverflowMenu(isArchived: Boolean, onArchive: () -> Unit, onRestore: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PpIconButton(PortProofIcons.MoreVertical, stringResource(UiR.string.ui_action_more), { expanded = true })
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
private fun MenuItem(text: String, icon: ImageVector, destructive: Boolean = false, onClick: () -> Unit) {
    val color = if (destructive) PortProofTheme.colors.dangerText else PortProofTheme.colors.textPrimary
    DropdownMenuItem(
        text = { Text(text, style = MaterialTheme.typography.bodyMedium, color = color) },
        leadingIcon = { Icon(icon, null, tint = color, modifier = Modifier.size(18.dp)) },
        onClick = onClick,
    )
}

@Composable
private fun DetailContent(
    state: ProjectDetailUiState.Success,
    onStatusSelected: (ProjectStatus) -> Unit,
    onRestore: () -> Unit,
) {
    val spacing = PortProofTheme.spacing
    val project = state.project
    LazyColumn(
        modifier = Modifier
            .widthIn(max = spacing.maxContentWidth)
            .fillMaxWidth(),
        contentPadding = PaddingValues(start = spacing.screenGutter, end = spacing.screenGutter, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
    ) {
        if (project.isArchived) item(key = "archived") { ArchivedBanner(onRestore) }
        item(key = "hero") { HeroCard(project) }
        item(key = "status") { StatusCard(project, onStatusSelected) }
        item(key = "schedule") { ScheduleCard(project, state.schedule) }
        project.description?.let { description -> item(key = "description") { DescriptionCard(description) } }
        item(key = "activity") { ActivityCard(state) }
    }
}

@Composable
private fun ArchivedBanner(onRestore: () -> Unit) {
    val colors = PortProofTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.warningContainer, PortProofTheme.shapes.card)
            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(PortProofIcons.Archive, null, tint = colors.warningText, modifier = Modifier.size(18.dp))
        Text(
            stringResource(R.string.project_archived_banner),
            style = MaterialTheme.typography.bodySmall,
            color = colors.warningText,
            modifier = Modifier.weight(1f).padding(vertical = 6.dp),
        )
        androidx.compose.material3.TextButton(onClick = onRestore) {
            Text(stringResource(UiR.string.ui_action_restore), color = colors.warningText, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun HeroCard(project: Project) {
    val colors = PortProofTheme.colors
    val notSet = stringResource(R.string.project_value_not_set)
    PpCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(project.name, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                project.reference?.let {
                    Text("# $it", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
            }
            Spacer(Modifier.size(8.dp))
            ProjectStatusPill(project)
        }
        Spacer(Modifier.height(16.dp))
        PpDivider()
        Spacer(Modifier.height(12.dp))
        InfoLine(PortProofIcons.Building, stringResource(R.string.project_label_customer), project.customerName ?: notSet)
        InfoLine(PortProofIcons.MapPin, stringResource(R.string.project_label_site), project.siteName ?: notSet)
        project.siteAddress?.let { InfoLine(PortProofIcons.FileText, stringResource(R.string.project_label_address), it) }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = PortProofTheme.colors.textMuted, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        PpLabeledValue(label, value, Modifier.weight(1f))
    }
}

@Composable
private fun StatusCard(project: Project, onStatusSelected: (ProjectStatus) -> Unit) {
    PpCard {
        PpSectionHeader(stringResource(R.string.project_section_status), style = MaterialTheme.typography.titleSmall)
        PpSegmentedControl(
            options = ProjectStatus.entries,
            selected = project.status,
            onSelect = onStatusSelected,
            label = { stringResource(it.labelRes) },
            enabled = !project.isArchived,
        )
    }
}

@Composable
private fun ScheduleCard(project: Project, schedule: ScheduleState?) {
    val colors = PortProofTheme.colors
    val notSet = stringResource(R.string.project_value_not_set)
    PpCard {
        PpSectionHeader(stringResource(R.string.project_section_schedule), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PpLabeledValue(stringResource(R.string.project_label_start), project.startDate?.let { formatDate(it) } ?: notSet, Modifier.weight(1f))
            PpLabeledValue(stringResource(R.string.project_label_end), project.endDate?.let { formatDate(it) } ?: notSet, Modifier.weight(1f))
        }
        if (schedule != null) {
            Spacer(Modifier.height(14.dp))
            val (label, color) = when (schedule) {
                is ScheduleState.Upcoming ->
                    pluralStringResource(R.plurals.project_schedule_not_started, schedule.daysUntilStart.toInt(), schedule.daysUntilStart) to colors.accent
                is ScheduleState.Running ->
                    stringResource(R.string.project_schedule_progress, schedule.day, schedule.totalDays) to colors.accent
                is ScheduleState.Overdue ->
                    pluralStringResource(R.plurals.project_schedule_overdue, schedule.daysOver.toInt(), schedule.daysOver) to colors.danger
                ScheduleState.Done -> stringResource(R.string.project_schedule_done) to colors.success
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))
            PpHatchedProgressBar(progress = schedule.progress, color = color)
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    PpCard {
        PpSectionHeader(stringResource(R.string.project_section_description), style = MaterialTheme.typography.titleSmall)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = PortProofTheme.colors.textPrimary)
    }
}

@Composable
private fun ActivityCard(state: ProjectDetailUiState.Success) {
    PpCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        PpSectionHeader(stringResource(R.string.project_section_activity), style = MaterialTheme.typography.titleSmall)
        if (state.activity.isEmpty()) {
            Text(
                stringResource(R.string.project_no_activity),
                style = MaterialTheme.typography.bodySmall,
                color = PortProofTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        state.activity.forEach { entry -> ActivityRow(entry, showProject = false) }
    }
}
