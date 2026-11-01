package com.portproof.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpSearchField
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.ui.LocalBottomBarInset
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.ui.ProjectCard

@Composable
internal fun SearchRoute(onOpenProject: (ProjectId) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(state, viewModel.query, viewModel::onQueryChange, onOpenProject)
}

@Composable
internal fun SearchScreen(
    state: SearchUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenProject: (ProjectId) -> Unit,
) {
    val spacing = PortProofTheme.spacing

    Column(Modifier.fillMaxSize().imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        PpTopBar(title = stringResource(R.string.search_title), modifier = Modifier.widthIn(max = spacing.maxContentWidth))
        PpSearchField(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.search_placeholder),
            modifier = Modifier
                .widthIn(max = spacing.maxContentWidth)
                .padding(horizontal = spacing.screenGutter, vertical = spacing.sm),
        )
        when (state) {
            SearchUiState.Idle -> PpEmptyState(
                icon = PortProofIcons.Search,
                title = stringResource(R.string.search_hint_title),
                message = stringResource(R.string.search_hint_message),
            )
            is SearchUiState.Results -> if (state.projects.isEmpty()) {
                PpEmptyState(
                    icon = PortProofIcons.Search,
                    title = stringResource(R.string.search_no_results_title),
                    message = stringResource(R.string.search_no_results_message, state.query),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.widthIn(max = spacing.maxContentWidth).fillMaxWidth(),
                    contentPadding = PaddingValues(start = spacing.screenGutter, end = spacing.screenGutter, top = spacing.sm, bottom = LocalBottomBarInset.current + spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
                ) {
                    item(key = "count") {
                        Text(
                            pluralStringResource(R.plurals.search_result_count, state.projects.size, state.projects.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = PortProofTheme.colors.textSecondary,
                        )
                    }
                    items(state.projects, key = { it.id.value }) { project ->
                        ProjectCard(project, onClick = { onOpenProject(project.id) })
                    }
                }
            }
        }
    }
}
