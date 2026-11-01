package com.portproof.feature.insights

import com.portproof.core.model.ActivityAction
import com.portproof.core.model.ActivityEntityType
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.TestActivityRepository
import com.portproof.core.testing.TestInstant
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
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 1)
    private val clock = Clock.fixed(TestInstant, ZoneOffset.UTC)
    private val projects = TestProjectRepository(
        listOf(
            testProject(id = "late", status = ProjectStatus.IN_PROGRESS).copy(endDate = today.minusDays(1)),
            testProject(id = "soon", status = ProjectStatus.PLANNED).copy(endDate = today.plusDays(6)),
            testProject(id = "later", status = ProjectStatus.PLANNED).copy(endDate = today.plusDays(7)),
            testProject(id = "done", status = ProjectStatus.COMPLETED),
        ),
    )
    private val activity = TestActivityRepository()

    @Test
    fun overviewCombinesCountsActivityAndDeadlinesOverdueFirst() = runTest {
        activity.hourly.value = List(24) { if (it == 9) 4 else 0 }
        val vm = OverviewViewModel(projects, activity, clock)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(4, state.summary.total)
        assertEquals(listOf("late", "soon"), state.deadlines.map { it.id.value })
        assertEquals(setOf(9), state.insights?.peakHours())
        assertEquals(today, state.today)
        assertNotNull(state.insights)
    }

    @Test
    fun activityLogExposesEntriesNewestFirst() = runTest {
        activity.entries.value = listOf(
            ActivityEntry(2, ProjectId("p"), "P", ActivityEntityType.PROJECT, "p", ActivityAction.UPDATED, null, null, TestInstant),
            ActivityEntry(1, ProjectId("p"), "P", ActivityEntityType.PROJECT, "p", ActivityAction.CREATED, null, null, TestInstant),
        )
        val vm = ActivityLogViewModel(activity)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertEquals(listOf(2L, 1L), vm.uiState.value.entries.map { it.id })
    }
}
