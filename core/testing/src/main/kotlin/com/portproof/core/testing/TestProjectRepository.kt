package com.portproof.core.testing

import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectDraft
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ProjectSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [ProjectRepository] for ViewModel tests. Set [failWrites] to simulate storage errors. */
class TestProjectRepository(initial: List<Project> = emptyList()) : ProjectRepository {
    private val projects = MutableStateFlow(initial)
    var failWrites = false
    val created = mutableListOf<ProjectDraft>()
    private var nextId = 1

    fun setProjects(value: List<Project>) = projects.update { value }

    override fun observeProjects(filter: ProjectListFilter, sort: ProjectSort, query: String): Flow<List<Project>> =
        projects.map { all ->
            all.filter { p ->
                when (filter) {
                    ProjectListFilter.ALL -> !p.isArchived
                    ProjectListFilter.ACTIVE -> !p.isArchived && p.status.isOpen
                    ProjectListFilter.COMPLETED -> !p.isArchived && !p.status.isOpen
                    ProjectListFilter.ARCHIVED -> p.isArchived
                }
            }.filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
        }

    override fun observeProject(id: ProjectId): Flow<Project?> = projects.map { all -> all.firstOrNull { it.id == id } }

    override fun observeRecentProjects(limit: Int): Flow<List<Project>> =
        projects.map { all -> all.filterNot { it.isArchived }.sortedByDescending { it.updatedAt }.take(limit) }

    override fun observeSummary(): Flow<ProjectSummary> = projects.map { all ->
        val live = all.filterNot { it.isArchived }
        ProjectSummary(
            total = live.size,
            active = live.count { it.status.isOpen },
            completed = live.count { !it.status.isOpen },
            archived = all.count { it.isArchived },
            planned = live.count { it.status == ProjectStatus.PLANNED },
            inProgress = live.count { it.status == ProjectStatus.IN_PROGRESS },
            onHold = live.count { it.status == ProjectStatus.ON_HOLD },
            createdThisMonth = live.size,
            completedThisMonth = 0,
        )
    }

    override fun searchProjects(query: String, limit: Int): Flow<List<Project>> = projects.map { all ->
        if (query.isBlank()) emptyList() else all.filter { it.name.contains(query.trim(), ignoreCase = true) }.take(limit)
    }

    override suspend fun getProject(id: ProjectId): Project? = projects.value.firstOrNull { it.id == id }

    override suspend fun createProject(draft: ProjectDraft): ProjectId {
        checkWritable()
        val id = ProjectId("created-${nextId++}")
        created += draft
        projects.update { it + draft.toProject(id) }
        return id
    }

    override suspend fun updateProject(id: ProjectId, draft: ProjectDraft): Boolean =
        mutate(id) { draft.toProject(id).copy(createdAt = it.createdAt, archivedAt = it.archivedAt) }

    override suspend fun setStatus(id: ProjectId, status: ProjectStatus): Boolean = mutate(id) { it.copy(status = status) }

    override suspend fun archiveProject(id: ProjectId): Boolean = mutate(id) { it.copy(archivedAt = TestInstant) }

    override suspend fun restoreProject(id: ProjectId): Boolean = mutate(id) { it.copy(archivedAt = null) }

    override suspend fun deleteProject(id: ProjectId): Boolean {
        checkWritable()
        val existed = projects.value.any { it.id == id }
        projects.update { all -> all.filterNot { it.id == id } }
        return existed
    }

    private fun mutate(id: ProjectId, transform: (Project) -> Project): Boolean {
        checkWritable()
        if (projects.value.none { it.id == id }) return false
        projects.update { all -> all.map { if (it.id == id) transform(it) else it } }
        return true
    }

    private fun checkWritable() {
        if (failWrites) throw java.io.IOException("Simulated storage failure")
    }
}

private fun ProjectDraft.toProject(id: ProjectId) = Project(
    id = id, name = name, reference = reference, customerName = customerName, siteName = siteName,
    siteAddress = siteAddress, description = description, status = status, startDate = startDate,
    endDate = endDate, createdAt = TestInstant, updatedAt = TestInstant, completedAt = null, archivedAt = null,
)
