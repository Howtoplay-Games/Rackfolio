package com.portproof.feature.dashboard

import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.UserPreferences
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.TestProjectRepository
import com.portproof.core.testing.TestUserPreferencesRepository
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
    private val preferences = TestUserPreferencesRepository(UserPreferences(technicianName = "  Sam Taylor "))
    private val clock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 11, 1)

    @Test
    fun firstRunIsEmptyAndShowsTheTrimmedName() = runTest {
        val vm = DashboardViewModel(projects, preferences, clock)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.summary.isEmpty)
        assertEquals("Sam Taylor", vm.uiState.value.technicianName)
    }

    @Test
    fun summaryFeedsTheTilesAndTheDateComesFromTheClock() = runTest {
        projects.setProjects(
            listOf(
                testProject(id = "a", status = ProjectStatus.IN_PROGRESS),
                testProject(id = "b", status = ProjectStatus.COMPLETED),
                testProject(id = "c", archived = true),
            ),
        )
        val vm = DashboardViewModel(projects, preferences, clock)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(2, state.summary.total)
        assertEquals(1, state.summary.active)
        assertEquals(1, state.summary.completed)
        assertEquals(today, state.today)
        assertEquals("Sam", state.firstName)
    }
}
