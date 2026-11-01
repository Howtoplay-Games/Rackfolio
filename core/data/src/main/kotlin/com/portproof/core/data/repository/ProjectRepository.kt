package com.portproof.core.data.repository

import com.portproof.core.model.Project
import com.portproof.core.model.ProjectDraft
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ProjectSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Offline-first access to projects. Room is the source of truth; every `observe*` flow re-emits
 * when the underlying tables change. Mutations return `false` when the project no longer exists;
 * storage failures surface as exceptions for the caller to report.
 */
interface ProjectRepository {
    fun observeProjects(filter: ProjectListFilter, sort: ProjectSort, query: String = ""): Flow<List<Project>>
    fun observeProject(id: ProjectId): Flow<Project?>
    fun observeRecentProjects(limit: Int): Flow<List<Project>>
    fun observeSummary(): Flow<ProjectSummary>

    /** Searches every project, including archived ones. Blank queries return an empty list. */
    fun searchProjects(query: String, limit: Int): Flow<List<Project>>

    /** Open projects due on or before [until], including overdue ones, earliest first. */
    fun observeUpcomingDeadlines(until: LocalDate, limit: Int): Flow<List<Project>>

    suspend fun getProject(id: ProjectId): Project?
    suspend fun createProject(draft: ProjectDraft): ProjectId
    suspend fun updateProject(id: ProjectId, draft: ProjectDraft): Boolean
    suspend fun setStatus(id: ProjectId, status: ProjectStatus): Boolean
    suspend fun archiveProject(id: ProjectId): Boolean
    suspend fun restoreProject(id: ProjectId): Boolean

    /** Permanently deletes the project and everything recorded under it. */
    suspend fun deleteProject(id: ProjectId): Boolean
}
