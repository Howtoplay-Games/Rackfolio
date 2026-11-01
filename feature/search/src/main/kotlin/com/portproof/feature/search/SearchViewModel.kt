package com.portproof.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.model.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SearchUiState {
    /** No query typed yet. */
    data object Idle : SearchUiState
    data class Results(val query: String, val projects: List<Project>) : SearchUiState
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    projectRepository: ProjectRepository,
) : ViewModel() {

    /** Compose state for the text field (synchronous updates while typing). */
    var query by mutableStateOf(savedStateHandle.get<String>(KEY_QUERY).orEmpty())
        private set

    /** Mirror of [query] that drives the search pipeline independently of Compose snapshots. */
    private val queryFlow = MutableStateFlow(query)

    val uiState: StateFlow<SearchUiState> = queryFlow
        .map { it.trim() }
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            projectRepository.searchProjects(q, RESULT_LIMIT).map<List<Project>, SearchUiState> { projects ->
                if (q.isEmpty()) SearchUiState.Idle else SearchUiState.Results(q, projects)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState.Idle)

    fun onQueryChange(value: String) {
        query = value
        queryFlow.value = value
        savedStateHandle[KEY_QUERY] = value
    }

    private companion object {
        const val KEY_QUERY = "search.query"
        const val DEBOUNCE_MS = 200L
        const val RESULT_LIMIT = 50
    }
}
