package com.portproof.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.database.model.ActivityEntity
import com.portproof.core.database.model.ProjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class ProjectDaoTest {

    private lateinit var db: PortProofDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PortProofDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    private fun project(
        id: String,
        name: String = id,
        status: String = "PLANNED",
        updatedAt: Long = 0,
        archivedAt: Long? = null,
        startDate: Long? = null,
        customer: String? = null,
        createdAt: Long = 0,
        completedAt: Long? = null,
    ) = ProjectEntity(
        id = id, name = name, reference = null, customerName = customer, siteName = null, siteAddress = null,
        description = null, status = status, startDateEpochDay = startDate, endDateEpochDay = null,
        createdAt = createdAt, updatedAt = updatedAt, completedAt = completedAt, archivedAt = archivedAt,
    )

    private suspend fun ids(filter: String, sort: String = "RECENTLY_UPDATED", pattern: String? = null) =
        db.projectDao().observeProjects(filter, sort, pattern).first().map { it.id }

    @Test
    fun filtersSeparateActiveCompletedAndArchived() = runTest {
        val dao = db.projectDao()
        dao.insert(project("a", status = "IN_PROGRESS", updatedAt = 3))
        dao.insert(project("b", status = "COMPLETED", updatedAt = 2))
        dao.insert(project("c", status = "COMPLETED", archivedAt = 5, updatedAt = 1))

        assertEquals(listOf("a", "b"), ids("ALL"))
        assertEquals(listOf("a"), ids("ACTIVE"))
        assertEquals(listOf("b"), ids("COMPLETED"))
        assertEquals(listOf("c"), ids("ARCHIVED"))
    }

    @Test
    fun sortsByNameCaseInsensitivelyAndStartDateWithNullsLast() = runTest {
        val dao = db.projectDao()
        dao.insert(project("1", name = "beta", startDate = null))
        dao.insert(project("2", name = "Alpha", startDate = 200))
        dao.insert(project("3", name = "gamma", startDate = 100))

        assertEquals(listOf("2", "1", "3"), ids("ALL", sort = "NAME"))
        assertEquals(listOf("3", "2", "1"), ids("ALL", sort = "START_DATE"))
    }

    @Test
    fun likeWildcardsInUserInputAreMatchedLiterally() = runTest {
        val dao = db.projectDao()
        dao.insert(project("pct", name = "Upgrade 50% done"))
        dao.insert(project("other", name = "Upgrade 500 cameras"))
        dao.insert(project("cust", name = "Rack", customer = "North_East Ltd"))

        assertEquals(listOf("pct"), ids("ALL", pattern = SqlLike.containsPattern("50%")))
        assertEquals(listOf("cust"), ids("ALL", pattern = SqlLike.containsPattern("h_e")))
        assertEquals(setOf("pct", "other"), ids("ALL", pattern = SqlLike.containsPattern("upgrade")).toSet())
        assertNull(SqlLike.containsPattern("   "))
    }

    @Test
    fun countsAddUpAndRespectTheMonthBoundary() = runTest {
        val dao = db.projectDao()
        dao.insert(project("p", status = "PLANNED", createdAt = 1_000))
        dao.insert(project("i", status = "IN_PROGRESS", createdAt = 50))
        dao.insert(project("h", status = "ON_HOLD", createdAt = 50))
        dao.insert(project("c", status = "COMPLETED", createdAt = 50, completedAt = 2_000))
        dao.insert(project("x", status = "COMPLETED", archivedAt = 10, createdAt = 50, completedAt = 20))

        val counts = dao.observeCounts(since = 500).first()
        assertEquals(4, counts.total)
        assertEquals(3, counts.active)
        assertEquals(1, counts.completed)
        assertEquals(1, counts.archived)
        assertEquals(1, counts.planned)
        assertEquals(1, counts.inProgress)
        assertEquals(1, counts.onHold)
        assertEquals(1, counts.createdThisMonth)
        assertEquals(1, counts.completedThisMonth)
    }

    @Test
    fun countsOnEmptyTableAreZero() = runTest {
        assertEquals(0, db.projectDao().observeCounts(since = 0).first().total)
    }

    @Test
    fun deletingProjectCascadesToActivity() = runTest {
        db.projectDao().insert(project("a"))
        db.activityDao().insert(activity("a"))
        assertEquals(1, db.activityDao().observeRecent(10).first().size)

        assertEquals(1, db.projectDao().deleteById("a"))
        assertEquals(0, db.activityDao().observeRecent(10).first().size)
    }

    @Test
    fun activityForUnknownProjectIsRejected() = runTest {
        assertFailsWith<android.database.sqlite.SQLiteConstraintException> {
            db.activityDao().insert(activity("missing"))
        }
    }

    @Test
    fun activityJoinsCurrentProjectName() = runTest {
        db.projectDao().insert(project("a", name = "Old name"))
        db.activityDao().insert(activity("a"))
        db.projectDao().update(project("a", name = "New name"))
        assertEquals("New name", db.activityDao().observeRecent(10).first().single().projectName)
    }

    private fun activity(projectId: String) = ActivityEntity(
        projectId = projectId, entityType = "PROJECT", entityId = projectId, action = "CREATED",
        label = null, detail = null, occurredAt = 1,
    )
}
