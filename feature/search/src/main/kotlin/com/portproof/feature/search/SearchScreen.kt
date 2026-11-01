package com.portproof.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpIconButtonShape
import com.portproof.core.designsystem.component.PpSearchField
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.ui.ProjectCard
import com.portproof.core.designsystem.R as DsR

@Composable
internal fun SearchRoute(onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(state, viewModel.query, viewModel::onQueryChange, onBack, onOpenProject)
}

@Composable
internal fun SearchScreen(
    state: SearchUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenProject: (ProjectId) -> Unit,
) {
    val spacing = PortProofTheme.spacing
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (query.isEmpty()) focusRequester.requestFocus() }

    Column(Modifier.fillMaxSize().imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .widthIn(max = spacing.maxContentWidth)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 12.dp, end = spacing.screenGutter, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PpIconButton(PortProofIcons.Back, stringResource(DsR.string.designsystem_back), onBack, shape = PpIconButtonShape.Circle)
            PpSearchField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = stringResource(R.string.search_placeholder),
                modifier = Modifier.weight(1f),
                fieldModifier = Modifier.focusRequester(focusRequester),
            )
        }
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
                    contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.sm),
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
