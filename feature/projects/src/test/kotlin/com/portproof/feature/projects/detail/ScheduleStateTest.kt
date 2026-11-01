package com.portproof.feature.projects.detail

import com.portproof.core.model.scheduleStateOf
import com.portproof.core.model.ScheduleState
import com.portproof.core.model.ProjectStatus
import com.portproof.core.testing.testProject
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScheduleStateTest {

    private val start = LocalDate.of(2026, 11, 1)
    private val end = LocalDate.of(2026, 11, 10)
    private val project = testProject().copy(startDate = start, endDate = end)

    @Test
    fun requiresBothDates() {
        assertNull(scheduleStateOf(project.copy(endDate = null), start))
    }

    @Test
    fun upcomingRunningAndOverdue() {
        assertEquals(ScheduleState.Upcoming(3), scheduleStateOf(project, start.minusDays(3)))
        assertEquals(ScheduleState.Running(day = 1, totalDays = 10), scheduleStateOf(project, start))
        assertEquals(ScheduleState.Running(day = 10, totalDays = 10), scheduleStateOf(project, end))
        assertEquals(ScheduleState.Overdue(2), scheduleStateOf(project, end.plusDays(2)))
    }

    @Test
    fun completedProjectsAreDoneRegardlessOfDates() {
        assertEquals(ScheduleState.Done, scheduleStateOf(project.copy(status = ProjectStatus.COMPLETED), end.plusDays(30)))
    }
}
