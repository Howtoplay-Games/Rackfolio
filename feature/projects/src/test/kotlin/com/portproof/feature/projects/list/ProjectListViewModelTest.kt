package com.portproof.feature.projects.list

import androidx.lifecycle.SavedStateHandle
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectStatus
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.TestProjectRepository
import com.portproof.core.testing.testProject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProjectRepository(
        listOf(
            testProject(id = "open", status = ProjectStatus.IN_PROGRESS),
            testProject(id = "done", status = ProjectStatus.COMPLETED),
            testProject(id = "old", status = ProjectStatus.COMPLETED, archived = true),
        ),
    )
    private val clock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun navigationArgumentPreselectsTheFilter() = runTest {
        val vm = ProjectListViewModel(SavedStateHandle(mapOf("filter" to "ARCHIVED")), repository, clock)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertEquals(ProjectListFilter.ARCHIVED, vm.uiState.value.filter)
        assertEquals(listOf("old"), vm.uiState.value.projects.map { it.id.value })
    }

    @Test
    fun changingFilterUpdatesTheList() = runTest {
        val vm = ProjectListViewModel(SavedStateHandle(), repository, clock)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertEquals(setOf("open", "done"), vm.uiState.value.projects.map { it.id.value }.toSet())

        vm.onFilterSelected(ProjectListFilter.ACTIVE)
        advanceUntilIdle()
        assertEquals(listOf("open"), vm.uiState.value.projects.map { it.id.value })
        assertEquals(1, vm.uiState.value.summary.archived)
    }

    @Test
    fun unknownFilterArgumentFallsBackToAll() = runTest {
        val vm = ProjectListViewModel(SavedStateHandle(mapOf("filter" to "BOGUS")), repository, clock)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertEquals(ProjectListFilter.ALL, vm.uiState.value.filter)
    }
}
