package com.portproof.feature.projects.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.model.ProjectSummary
import com.portproof.feature.projects.navigation.ProjectArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ProjectListUiState(
    val isLoading: Boolean = true,
    val filter: ProjectListFilter = ProjectListFilter.ALL,
    val sort: ProjectSort = ProjectSort.RECENTLY_UPDATED,
    val projects: List<Project> = emptyList(),
    val summary: ProjectSummary = ProjectSummary.Empty,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    projectRepository: ProjectRepository,
) : ViewModel() {

    /** Compose state (not a StateFlow) so the search field updates synchronously while typing. */
    var query by mutableStateOf(savedStateHandle.get<String>(KEY_QUERY).orEmpty())
        private set

    private val filter = savedStateHandle.getStateFlow(
        KEY_SELECTED_FILTER,
        ProjectListFilter.fromCode(savedStateHandle.get<String>(ProjectArgs.FILTER))?.code ?: ProjectListFilter.ALL.code,
    )
    private val sort = savedStateHandle.getStateFlow(KEY_SORT, ProjectSort.RECENTLY_UPDATED.code)

    /** Mirror of [query] that drives the list pipeline independently of Compose snapshots. */
    private val queryFlow = MutableStateFlow(query)
    private val debouncedQuery = queryFlow.debounce(QUERY_DEBOUNCE_MS).distinctUntilChanged()

    val uiState: StateFlow<ProjectListUiState> = combine(filter, sort, debouncedQuery) { f, s, q -> Triple(f, s, q) }
        .flatMapLatest { (filterCode, sortCode, q) ->
            val selectedFilter = ProjectListFilter.fromCode(filterCode) ?: ProjectListFilter.ALL
            val selectedSort = ProjectSort.fromCode(sortCode) ?: ProjectSort.RECENTLY_UPDATED
            combine(
                projectRepository.observeProjects(selectedFilter, selectedSort, q),
                projectRepository.observeSummary(),
            ) { projects, summary ->
                ProjectListUiState(
                    isLoading = false,
                    filter = selectedFilter,
                    sort = selectedSort,
                    projects = projects,
                    summary = summary,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectListUiState())

    fun onQueryChange(value: String) {
        query = value
        queryFlow.value = value
        savedStateHandle[KEY_QUERY] = value
    }

    fun onFilterSelected(value: ProjectListFilter) {
        savedStateHandle[KEY_SELECTED_FILTER] = value.code
    }

    fun onSortSelected(value: ProjectSort) {
        savedStateHandle[KEY_SORT] = value.code
    }

    private companion object {
        const val KEY_QUERY = "projects.query"
        const val KEY_SELECTED_FILTER = "projects.selectedFilter"
        const val KEY_SORT = "projects.sort"
        const val QUERY_DEBOUNCE_MS = 200L
    }
}
