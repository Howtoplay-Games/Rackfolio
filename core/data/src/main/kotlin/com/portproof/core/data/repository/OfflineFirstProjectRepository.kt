package com.portproof.core.data.repository

import androidx.room.withTransaction
import com.portproof.core.data.mapper.asExternalModel
import com.portproof.core.database.PortProofDatabase
import com.portproof.core.database.SqlLike
import com.portproof.core.database.dao.ActivityDao
import com.portproof.core.database.dao.ProjectDao
import com.portproof.core.database.model.ActivityEntity
import com.portproof.core.database.model.ProjectEntity
import com.portproof.core.model.ActivityAction
import com.portproof.core.model.ActivityEntityType
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectDraft
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ProjectSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Every mutation and its activity-log entry are written in one transaction. */
internal class OfflineFirstProjectRepository @Inject constructor(
    private val database: PortProofDatabase,
    private val projectDao: ProjectDao,
    private val activityDao: ActivityDao,
    private val clock: Clock,
) : ProjectRepository {

    override fun observeProjects(filter: ProjectListFilter, sort: ProjectSort, query: String): Flow<List<Project>> =
        projectDao.observeProjects(filter.code, sort.code, SqlLike.containsPattern(query))
            .map { rows -> rows.map(ProjectEntity::asExternalModel) }

    override fun observeProject(id: ProjectId): Flow<Project?> =
        projectDao.observeById(id.value).map { it?.asExternalModel() }

    override fun observeRecentProjects(limit: Int): Flow<List<Project>> =
        projectDao.observeRecent(limit).map { rows -> rows.map(ProjectEntity::asExternalModel) }

    override fun observeSummary(): Flow<ProjectSummary> {
        val monthStart = LocalDate.now(clock).withDayOfMonth(1).atStartOfDay(clock.zone).toInstant().toEpochMilli()
        return projectDao.observeCounts(since = monthStart).map { it.asExternalModel() }
    }

    override fun searchProjects(query: String, limit: Int): Flow<List<Project>> {
        val pattern = SqlLike.containsPattern(query) ?: return flowOf(emptyList())
        return projectDao.search(pattern, limit).map { rows -> rows.map(ProjectEntity::asExternalModel) }
    }

    override fun observeUpcomingDeadlines(until: LocalDate, limit: Int): Flow<List<Project>> =
        projectDao.observeDeadlines(until.toEpochDay(), limit).map { rows -> rows.map(ProjectEntity::asExternalModel) }

    override suspend fun getProject(id: ProjectId): Project? = projectDao.getById(id.value)?.asExternalModel()

    override suspend fun createProject(draft: ProjectDraft): ProjectId {
        val id = ProjectId.random()
        val now = clock.millis()
        database.withTransaction {
            projectDao.insert(draft.toNewEntity(id, now))
            activityDao.insert(activity(id, ActivityAction.CREATED, now))
        }
        return id
    }

    override suspend fun updateProject(id: ProjectId, draft: ProjectDraft): Boolean = database.withTransaction {
        val existing = projectDao.getById(id.value) ?: return@withTransaction false
        val now = clock.millis()
        val edited = existing.withDraft(draft)
        val detailsChanged = edited.copy(status = existing.status) != existing
        val statusChanged = edited.status != existing.status
        if (!detailsChanged && !statusChanged) return@withTransaction true

        projectDao.update(
            edited.copy(updatedAt = now, completedAt = completedAtFor(existing, draft.status, now)),
        )
        if (detailsChanged) activityDao.insert(activity(id, ActivityAction.UPDATED, now))
        if (statusChanged) activityDao.insert(activity(id, ActivityAction.STATUS_CHANGED, now, draft.status.code))
        true
    }

    override suspend fun setStatus(id: ProjectId, status: ProjectStatus): Boolean = database.withTransaction {
        val existing = projectDao.getById(id.value) ?: return@withTransaction false
        if (existing.status == status.code) return@withTransaction true
        val now = clock.millis()
        projectDao.updateStatus(id.value, status.code, completedAtFor(existing, status, now), now)
        activityDao.insert(activity(id, ActivityAction.STATUS_CHANGED, now, status.code))
        true
    }

    override suspend fun archiveProject(id: ProjectId): Boolean = setArchived(id, archived = true)

    override suspend fun restoreProject(id: ProjectId): Boolean = setArchived(id, archived = false)

    override suspend fun deleteProject(id: ProjectId): Boolean = projectDao.deleteById(id.value) > 0

    private suspend fun setArchived(id: ProjectId, archived: Boolean): Boolean = database.withTransaction {
        val existing = projectDao.getById(id.value) ?: return@withTransaction false
        if ((existing.archivedAt != null) == archived) return@withTransaction true
        val now = clock.millis()
        projectDao.updateArchivedAt(id.value, archivedAt = if (archived) now else null, updatedAt = now)
        activityDao.insert(activity(id, if (archived) ActivityAction.ARCHIVED else ActivityAction.RESTORED, now))
        true
    }

    /** Keeps the original completion time while a project stays completed; clears it when reopened. */
    private fun completedAtFor(existing: ProjectEntity, status: ProjectStatus, now: Long): Long? = when {
        status != ProjectStatus.COMPLETED -> null
        existing.status == ProjectStatus.COMPLETED.code -> existing.completedAt ?: now
        else -> now
    }

    private fun activity(id: ProjectId, action: ActivityAction, now: Long, detail: String? = null) = ActivityEntity(
        projectId = id.value,
        entityType = ActivityEntityType.PROJECT.code,
        entityId = id.value,
        action = action.code,
        label = null,
        detail = detail,
        occurredAt = now,
    )
}

private fun ProjectDraft.toNewEntity(id: ProjectId, now: Long) = ProjectEntity(
    id = id.value,
    name = name,
    reference = reference,
    customerName = customerName,
    siteName = siteName,
    siteAddress = siteAddress,
    description = description,
    status = status.code,
    startDateEpochDay = startDate?.toEpochDay(),
    endDateEpochDay = endDate?.toEpochDay(),
    createdAt = now,
    updatedAt = now,
    completedAt = if (status == ProjectStatus.COMPLETED) now else null,
    archivedAt = null,
)

private fun ProjectEntity.withDraft(draft: ProjectDraft) = copy(
    name = draft.name,
    reference = draft.reference,
    customerName = draft.customerName,
    siteName = draft.siteName,
    siteAddress = draft.siteAddress,
    description = draft.description,
    status = draft.status.code,
    startDateEpochDay = draft.startDate?.toEpochDay(),
    endDateEpochDay = draft.endDate?.toEpochDay(),
)
