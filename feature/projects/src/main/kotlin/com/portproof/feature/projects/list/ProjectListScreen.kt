package com.portproof.feature.projects.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.designsystem.component.PpSearchField
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.component.horizontalBleed
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.model.ProjectSummary
import com.portproof.core.ui.LocalBottomBarInset
import com.portproof.core.ui.ProjectCard
import com.portproof.feature.projects.R
import com.portproof.core.ui.R as UiR

@Composable
internal fun ProjectListRoute(
    onProjectClick: (ProjectId) -> Unit,
    onCreateProject: () -> Unit,
    viewModel: ProjectListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProjectListScreen(
        state = state,
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        onFilterSelected = viewModel::onFilterSelected,
        onSortSelected = viewModel::onSortSelected,
        onProjectClick = onProjectClick,
        onCreateProject = onCreateProject,
    )
}

@Composable
internal fun ProjectListScreen(
    state: ProjectListUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterSelected: (ProjectListFilter) -> Unit,
    onSortSelected: (ProjectSort) -> Unit,
    onProjectClick: (ProjectId) -> Unit,
    onCreateProject: () -> Unit,
) {
    val spacing = PortProofTheme.spacing
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = spacing.maxContentWidth)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = spacing.screenGutter,
                end = spacing.screenGutter,
                bottom = LocalBottomBarInset.current + spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
        ) {
            item(key = "header") { ProjectListHeader(state.summary, onCreateProject) }
            item(key = "search") { PpSearchField(query, onQueryChange, stringResource(R.string.projects_search_placeholder)) }
            item(key = "filters") {
                FilterRow(state.filter, state.summary, state.sort, onFilterSelected, onSortSelected)
            }
            if (!state.isLoading && state.projects.isEmpty()) {
                item(key = "empty") { EmptyProjects(state, query, onCreateProject) }
            }
            items(state.projects, key = { it.id.value }) { project ->
                ProjectCard(project = project, onClick = { onProjectClick(project.id) }, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun ProjectListHeader(summary: ProjectSummary, onCreateProject: () -> Unit) {
    PpTopBar(
        title = stringResource(R.string.projects_title),
        subtitle = pluralStringResource(R.plurals.projects_count, summary.total, summary.total),
        modifier = Modifier.horizontalBleed(PortProofTheme.spacing.screenGutter),
        actions = { PpIconButton(PortProofIcons.Add, stringResource(UiR.string.ui_action_new_project), onCreateProject) },
    )
}

@Composable
private fun EmptyProjects(state: ProjectListUiState, query: String, onCreateProject: () -> Unit) {
    when {
        query.isNotBlank() -> PpEmptyState(
            icon = PortProofIcons.Search,
            title = stringResource(R.string.projects_empty_filtered_title),
            message = stringResource(R.string.projects_empty_search_message, query.trim()),
        )
        state.summary.isEmpty -> PpEmptyState(
            icon = PortProofIcons.FolderOpen,
            title = stringResource(R.string.projects_empty_title),
            message = stringResource(R.string.projects_empty_message),
            action = {
                PpPrimaryButton(stringResource(UiR.string.ui_action_new_project), onCreateProject, leadingIcon = PortProofIcons.Add)
            },
        )
        else -> PpEmptyState(
            icon = PortProofIcons.Filter,
            title = stringResource(R.string.projects_empty_filtered_title),
            message = stringResource(R.string.projects_empty_filtered_message),
        )
    }
}

@Composable
private fun FilterRow(
    selected: ProjectListFilter,
    summary: ProjectSummary,
    sort: ProjectSort,
    onFilterSelected: (ProjectListFilter) -> Unit,
    onSortSelected: (ProjectSort) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ProjectListFilter.entries, key = { it.code }) { filter ->
                val count = when (filter) {
                    ProjectListFilter.ALL -> summary.total
                    ProjectListFilter.ACTIVE -> summary.active
                    ProjectListFilter.COMPLETED -> summary.completed
                    ProjectListFilter.ARCHIVED -> summary.archived
                }
                FilterPill("${stringResource(filter.labelRes)} $count", filter == selected) { onFilterSelected(filter) }
            }
        }
        SortMenu(sort, onSortSelected)
    }
}

@Composable
private fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = PortProofTheme.colors
    Surface(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        shape = PortProofTheme.shapes.pill,
        color = if (selected) colors.inverseSurface else colors.surface,
        border = if (selected) null else BorderStroke(1.dp, colors.border),
    ) {
        Box(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) colors.onInverseSurface else colors.textPrimary,
            )
        }
    }
}

@Composable
private fun SortMenu(sort: ProjectSort, onSortSelected: (ProjectSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PpIconButton(
            icon = PortProofIcons.Sort,
            contentDescription = stringResource(R.string.projects_sort_label),
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = PortProofTheme.colors.surface,
        ) {
            ProjectSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes), style = MaterialTheme.typography.bodyMedium) },
                    trailingIcon = if (option == sort) {
                        { Icon(PortProofIcons.Check, contentDescription = null) }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onSortSelected(option)
                    },
                )
            }
        }
    }
}

private val ProjectListFilter.labelRes: Int
    get() = when (this) {
        ProjectListFilter.ALL -> R.string.projects_filter_all
        ProjectListFilter.ACTIVE -> R.string.projects_filter_active
        ProjectListFilter.COMPLETED -> R.string.projects_filter_completed
        ProjectListFilter.ARCHIVED -> R.string.projects_filter_archived
    }

private val ProjectSort.labelRes: Int
    get() = when (this) {
        ProjectSort.RECENTLY_UPDATED -> R.string.projects_sort_recent
        ProjectSort.NAME -> R.string.projects_sort_name
        ProjectSort.START_DATE -> R.string.projects_sort_start_date
    }
