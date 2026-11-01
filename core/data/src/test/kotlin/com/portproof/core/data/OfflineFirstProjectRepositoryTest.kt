package com.portproof.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.data.repository.OfflineFirstActivityRepository
import com.portproof.core.data.repository.OfflineFirstProjectRepository
import com.portproof.core.database.PortProofDatabase
import com.portproof.core.model.ActivityAction
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class OfflineFirstProjectRepositoryTest {

    private val clock = TestClock()
    private lateinit var db: PortProofDatabase
    private lateinit var projects: OfflineFirstProjectRepository
    private lateinit var activity: OfflineFirstActivityRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PortProofDatabase::class.java)
            .build()
        projects = OfflineFirstProjectRepository(db, db.projectDao(), db.activityDao(), clock)
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
}
