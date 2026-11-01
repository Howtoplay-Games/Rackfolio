package com.portproof.feature.projects.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpCategoryTile
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpListScreen
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.designsystem.component.PpSearchField
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.component.PpTextLink
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.ui.ProjectCard
import com.portproof.feature.projects.R
import com.portproof.core.ui.R as UiR

@Composable
internal fun ProjectListRoute(
    onProjectClick: (ProjectId) -> Unit,
    onEditProject: (ProjectId) -> Unit,
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
        onEditProject = onEditProject,
        onCreateProject = onCreateProject,
    )
}

/** Projects tab, laid out as the reference's recipe screen: search, category tiles, counted section, cards. */
@Composable
internal fun ProjectListScreen(
    state: ProjectListUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterSelected: (ProjectListFilter) -> Unit,
    onSortSelected: (ProjectSort) -> Unit,
    onProjectClick: (ProjectId) -> Unit,
    onEditProject: (ProjectId) -> Unit,
    onCreateProject: () -> Unit,
) {
    val spacing = PortProofTheme.spacing
    PpListScreen {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = spacing.screenGutter, end = spacing.screenGutter, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            item(key = "search") {
                Row(
                    Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PpSearchField(query, onQueryChange, stringResource(R.string.projects_search_placeholder), Modifier.weight(1f))
                    SortMenu(state.sort, onSortSelected)
                }
            }
            item(key = "categories") { CategoryRow(state.filter, onFilterSelected) }
            item(key = "section") {
                PpSectionHeader(
                    title = stringResource(state.filter.sectionRes),
                    badge = if (state.isLoading) null else state.projects.size.toString(),
                    action = { PpTextLink(stringResource(R.string.projects_new_link), onCreateProject, color = PortProofTheme.colors.accentText) },
                )
            }
            if (!state.isLoading && state.projects.isEmpty()) {
                item(key = "empty") { EmptyProjects(state, query, onCreateProject) }
            }
            items(state.projects, key = { it.id.value }) { project ->
                ProjectCard(
                    project = project,
                    today = state.today,
                    onClick = { onProjectClick(project.id) },
                    onEdit = { onEditProject(project.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(selected: ProjectListFilter, onFilterSelected: (ProjectListFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(ProjectListFilter.entries, key = { it.code }) { filter ->
            PpCategoryTile(
                icon = filter.icon,
                label = stringResource(filter.labelRes),
                selected = filter == selected,
                onClick = { onFilterSelected(filter) },
            )
        }
    }
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
            action = { PpPrimaryButton(stringResource(UiR.string.ui_action_new_project), onCreateProject) },
        )
        else -> PpEmptyState(
            icon = PortProofIcons.Filter,
            title = stringResource(R.string.projects_empty_filtered_title),
            message = stringResource(R.string.projects_empty_filtered_message),
        )
    }
}

@Composable
private fun SortMenu(sort: ProjectSort, onSortSelected: (ProjectSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PpIconButton(PortProofIcons.Sort, stringResource(R.string.projects_sort_label), onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = PortProofTheme.colors.surface) {
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

private val ProjectListFilter.sectionRes: Int
    get() = when (this) {
        ProjectListFilter.ALL -> R.string.projects_section_all
        ProjectListFilter.ACTIVE -> R.string.projects_section_active
        ProjectListFilter.COMPLETED -> R.string.projects_section_completed
        ProjectListFilter.ARCHIVED -> R.string.projects_section_archived
    }

private val ProjectListFilter.icon
    get() = when (this) {
        ProjectListFilter.ALL -> PortProofIcons.Grid
        ProjectListFilter.ACTIVE -> PortProofIcons.Activity
        ProjectListFilter.COMPLETED -> PortProofIcons.CircleCheck
        ProjectListFilter.ARCHIVED -> PortProofIcons.Archive
    }

private val ProjectSort.labelRes: Int
    get() = when (this) {
        ProjectSort.RECENTLY_UPDATED -> R.string.projects_sort_recent
        ProjectSort.NAME -> R.string.projects_sort_name
        ProjectSort.START_DATE -> R.string.projects_sort_start_date
    }
