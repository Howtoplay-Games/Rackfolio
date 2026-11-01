package com.portproof.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.data.repository.OfflineFirstActivityRepository
import com.portproof.core.data.repository.OfflineFirstProjectRepository
import com.portproof.core.database.PortProofDatabase
import com.portproof.core.data.repository.PlanRepository
import com.portproof.core.model.ActiveProjectLimitException
import com.portproof.core.model.ActivityAction
import com.portproof.core.model.AppPlan
import com.portproof.core.model.ProjectDraft
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectInput
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ProjectValidationResult
import com.portproof.core.model.ProjectValidator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class OfflineFirstProjectRepositoryTest {

    private val clock = TestClock()
    private val plans = FixedPlanRepository(AppPlan.Pro)
    private lateinit var db: PortProofDatabase
    private lateinit var projects: OfflineFirstProjectRepository
    private lateinit var activity: OfflineFirstActivityRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PortProofDatabase::class.java)
            .build()
        projects = OfflineFirstProjectRepository(db, db.projectDao(), db.activityDao(), plans, clock)
        activity = OfflineFirstActivityRepository(db.activityDao(), clock)
    }

    @After
    fun tearDown() = db.close()

    private fun draft(name: String = "Office CCTV", status: ProjectStatus = ProjectStatus.PLANNED): ProjectDraft =
        (ProjectValidator.validate(ProjectInput(name = name, status = status)) as ProjectValidationResult.Valid).draft

    private suspend fun actions(id: ProjectId) = activity.observeProjectActivity(id, 50).first().map { it.action }

    @Test
    fun createStoresProjectAndRecordsActivity() = runTest {
        val id = projects.createProject(draft(name = "Warehouse network"))

        val stored = assertNotNull(projects.getProject(id))
        assertEquals("Warehouse network", stored.name)
        assertEquals(clock.now, stored.createdAt)
        assertEquals(listOf(ActivityAction.CREATED), actions(id))
        assertEquals("Warehouse network", activity.observeRecentActivity(5).first().single().projectName)
    }

    @Test
    fun completingSetsCompletionTimeAndReopeningClearsIt() = runTest {
        val id = projects.createProject(draft())
        clock.advanceMinutes(30)
        assertTrue(projects.setStatus(id, ProjectStatus.COMPLETED))
        assertEquals(clock.now, projects.getProject(id)?.completedAt)

        clock.advanceMinutes(30)
        projects.updateProject(id, draft(name = "Renamed", status = ProjectStatus.COMPLETED))
        assertEquals(clock.now.minusSeconds(30 * 60), projects.getProject(id)?.completedAt)

        projects.setStatus(id, ProjectStatus.IN_PROGRESS)
        assertNull(projects.getProject(id)?.completedAt)
        assertEquals(
            listOf(ActivityAction.STATUS_CHANGED, ActivityAction.UPDATED, ActivityAction.STATUS_CHANGED, ActivityAction.CREATED),
            actions(id),
        )
    }

    @Test
    fun unchangedEditDoesNotTouchTimestampsOrActivity() = runTest {
        val id = projects.createProject(draft())
        clock.advanceMinutes(5)
        assertTrue(projects.updateProject(id, draft()))
        assertEquals(Instant.parse("2026-11-01T10:00:00Z"), projects.getProject(id)?.updatedAt)
        assertEquals(listOf(ActivityAction.CREATED), actions(id))
    }

    @Test
    fun archiveAndRestoreMoveProjectBetweenFilters() = runTest {
        val id = projects.createProject(draft())
        assertTrue(projects.archiveProject(id))
        assertTrue(projects.observeProjects(ProjectListFilter.ALL, ProjectSort.NAME).first().isEmpty())
        assertEquals(id, projects.observeProjects(ProjectListFilter.ARCHIVED, ProjectSort.NAME).first().single().id)
        assertEquals(1, projects.observeSummary().first().archived)

        assertTrue(projects.restoreProject(id))
        assertEquals(1, projects.observeSummary().first().total)
        assertEquals(listOf(ActivityAction.RESTORED, ActivityAction.ARCHIVED, ActivityAction.CREATED), actions(id))
    }

    @Test
    fun deleteRemovesProjectAndItsActivity() = runTest {
        val id = projects.createProject(draft())
        assertTrue(projects.deleteProject(id))
        assertNull(projects.getProject(id))
        assertTrue(activity.observeRecentActivity(10).first().isEmpty())
    }

    @Test
    fun mutationsOnMissingProjectReturnFalse() = runTest {
        val missing = ProjectId("does-not-exist")
        assertFalse(projects.updateProject(missing, draft()))
        assertFalse(projects.setStatus(missing, ProjectStatus.COMPLETED))
        assertFalse(projects.archiveProject(missing))
        assertFalse(projects.deleteProject(missing))
    }

    @Test
    fun searchIncludesArchivedProjectsAndIgnoresBlankQueries() = runTest {
        val id = projects.createProject(draft(name = "Hospital access control"))
        projects.archiveProject(id)
        assertEquals(id, projects.searchProjects("access", 10).first().single().id)
        assertTrue(projects.searchProjects("  ", 10).first().isEmpty())
    }

    @Test
    fun activityTrendBucketsByLocalDay() = runTest {
        clock.now = Instant.parse("2026-10-30T09:00:00Z")
        projects.createProject(draft(name = "A")) // 3 days ago -> in the current 7-day period
        clock.now = Instant.parse("2026-10-20T09:00:00Z")
        projects.createProject(draft(name = "B")) // 12 days ago -> previous period
        clock.now = Instant.parse("2026-11-02T08:00:00Z")

        val trend = activity.observeActivityTrend(days = 7).first()
        assertEquals(7, trend.days.size)
        assertEquals(LocalDate.of(2026, 11, 2), trend.days.last().date)
        assertEquals(1, trend.days.single { it.date == LocalDate.of(2026, 10, 30) }.count)
        assertEquals(1, trend.total)
        assertEquals(1, trend.previousPeriodTotal)
    }

    @Test
    fun insightsCountEntriesPerLocalHourWithinThePeriod() = runTest {
        clock.now = Instant.parse("2026-11-01T08:15:00Z")
        val id = projects.createProject(draft(name = "A"))
        clock.now = Instant.parse("2026-11-01T08:45:00Z")
        projects.setStatus(id, ProjectStatus.IN_PROGRESS)
        clock.now = Instant.parse("2026-10-30T14:05:00Z")
        projects.createProject(draft(name = "B"))
        clock.now = Instant.parse("2026-10-20T14:05:00Z") // outside the 7-day period
        projects.createProject(draft(name = "C"))
        clock.now = Instant.parse("2026-11-01T18:00:00Z")

        val insights = activity.observeActivityInsights(days = 7).first()
        assertEquals(2, insights.byHour[8])
        assertEquals(1, insights.byHour[14])
        assertEquals(3, insights.byHour.sum())
        assertEquals(2, insights.today)
        assertEquals(setOf(8, 14), insights.peakHours())
    }

    @Test
    fun upcomingDeadlinesExcludeCompletedAndLaterProjects() = runTest {
        val due = (ProjectValidator.validate(
            ProjectInput(name = "Due", status = ProjectStatus.IN_PROGRESS, endDate = LocalDate.of(2026, 11, 3)),
        ) as ProjectValidationResult.Valid).draft
        val later = (ProjectValidator.validate(ProjectInput(name = "Later", endDate = LocalDate.of(2026, 12, 1))) as ProjectValidationResult.Valid).draft
        projects.createProject(due)
        projects.createProject(later)
        assertEquals(listOf("Due"), projects.observeUpcomingDeadlines(LocalDate.of(2026, 11, 7), 10).first().map { it.name })
    }

    @Test
    fun freePlanBlocksOneActiveProjectTooManyAndWritesNothing() = runTest {
        plans.value = AppPlan.Free(activeProjectLimit = 2)
        projects.createProject(draft(name = "One"))
        projects.createProject(draft(name = "Two"))

        val error = assertFailsWith<ActiveProjectLimitException> { projects.createProject(draft(name = "Three")) }
        assertEquals(2, error.limit)
        assertEquals(2, projects.observeSummary().first().active)
        assertEquals(2, activity.observeRecentActivity(10).first().size)

        // Completed projects do not count, so they can still be recorded.
        projects.createProject(draft(name = "Done", status = ProjectStatus.COMPLETED))
        assertEquals(3, projects.observeSummary().first().total)
    }

    @Test
    fun freePlanBlocksReopeningAndRestoringAtTheLimit() = runTest {
        val done = projects.createProject(draft(name = "Done", status = ProjectStatus.COMPLETED))
        val archived = projects.createProject(draft(name = "Archived"))
        projects.archiveProject(archived)
        projects.createProject(draft(name = "Open"))
        plans.value = AppPlan.Free(activeProjectLimit = 1)

        assertFailsWith<ActiveProjectLimitException> { projects.setStatus(done, ProjectStatus.IN_PROGRESS) }
        assertFailsWith<ActiveProjectLimitException> { projects.updateProject(done, draft(name = "Done", status = ProjectStatus.ON_HOLD)) }
        assertFailsWith<ActiveProjectLimitException> { projects.restoreProject(archived) }
        assertEquals(ProjectStatus.COMPLETED, projects.getProject(done)?.status)
        assertNotNull(projects.getProject(archived)?.archivedAt)

        // Edits that keep the active count unchanged still work.
        assertTrue(projects.updateProject(done, draft(name = "Done and renamed", status = ProjectStatus.COMPLETED)))

        plans.value = AppPlan.Pro
        assertTrue(projects.restoreProject(archived))
        assertTrue(projects.setStatus(done, ProjectStatus.IN_PROGRESS))
        assertEquals(3, projects.observeSummary().first().active)
    }
}

/** Plan source whose value each test sets directly. */
private class FixedPlanRepository(var value: AppPlan) : PlanRepository {
    override val plan get() = kotlinx.coroutines.flow.flowOf(value)
    override suspend fun currentPlan(): AppPlan = value
}
