package com.portproof.feature.dashboard

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
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val projects = TestProjectRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 11, 1)

    @Test
    fun firstRunIsEmpty() = runTest {
        val vm = DashboardViewModel(projects, clock)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.summary.isEmpty)
        assertEquals(emptyList(), vm.uiState.value.recentProjects)
    }

    @Test
    fun gaugeCountsAndRecentCardsComeFromTheRepository() = runTest {
        projects.setProjects(
            listOf(
                testProject(id = "a", status = ProjectStatus.IN_PROGRESS, updatedAt = Instant.parse("2026-11-01T09:00:00Z")),
                testProject(id = "b", status = ProjectStatus.COMPLETED, updatedAt = Instant.parse("2026-10-30T09:00:00Z")),
                testProject(id = "c", archived = true),
                testProject(id = "d", status = ProjectStatus.PLANNED, updatedAt = Instant.parse("2026-10-31T09:00:00Z")),
                testProject(id = "e", status = ProjectStatus.ON_HOLD, updatedAt = Instant.parse("2026-10-01T09:00:00Z")),
            ),
        )
        val vm = DashboardViewModel(projects, clock)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(4, state.summary.total)
        assertEquals(1, state.summary.completed)
        assertEquals(0.25f, state.summary.completionRatio)
        // Newest first, archived projects left out, at most three cards.
        assertEquals(listOf("a", "d", "b"), state.recentProjects.map { it.id.value })
        assertEquals(today, state.today)
    }
}
