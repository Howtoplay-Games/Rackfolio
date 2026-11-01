package com.portproof.feature.projects.detail

import com.portproof.core.model.scheduleStateOf
import com.portproof.core.model.ProjectStatus
import com.portproof.core.testing.testProject
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectReviewTest {

    private val today = LocalDate.of(2026, 11, 1)
    private val base = testProject(status = ProjectStatus.IN_PROGRESS)

    @Test
    fun completeRecordOnScheduleHasNoIssues() {
        val review = reviewOf(base.copy(startDate = today.minusDays(5), endDate = today.plusDays(20)), today)
        assertEquals(ScheduleCheck.OnTrack, review.schedule)
        assertEquals(0, review.issueCount)
    }

    @Test
    fun missingDetailsAreFlagged() {
        val review = reviewOf(base.copy(customerName = null, siteName = null, siteAddress = null), today)
        assertTrue(review.missingCustomer)
        assertTrue(review.missingSite)
        assertEquals(ScheduleCheck.NoDates, review.schedule)
        assertEquals(3, review.issueCount)
    }

    @Test
    fun overdueAndDueSoonUseTheCompletionDate() {
        assertEquals(ScheduleCheck.Overdue(2), reviewOf(base.copy(endDate = today.minusDays(2)), today).schedule)
        assertEquals(ScheduleCheck.DueSoon(0), reviewOf(base.copy(endDate = today), today).schedule)
        assertEquals(ScheduleCheck.DueSoon(6), reviewOf(base.copy(endDate = today.plusDays(6)), today).schedule)
        assertEquals(ScheduleCheck.OnTrack, reviewOf(base.copy(endDate = today.plusDays(7)), today).schedule)
    }

    @Test
    fun completedProjectsAreNeverOverdueAndFillTheBar() {
        val done = base.copy(status = ProjectStatus.COMPLETED, endDate = today.minusDays(30))
        assertEquals(ScheduleCheck.Done, reviewOf(done, today).schedule)
        assertEquals(5, progressSegments(done, scheduleStateOf(done, today)))
    }

    @Test
    fun onHoldAndArchivedCountAsIssues() {
        val review = reviewOf(base.copy(status = ProjectStatus.ON_HOLD, endDate = today.plusDays(30)).copy(archivedAt = base.createdAt), today)
        assertTrue(review.onHold)
        assertTrue(review.archived)
        assertEquals(2, review.issueCount)
    }
}
