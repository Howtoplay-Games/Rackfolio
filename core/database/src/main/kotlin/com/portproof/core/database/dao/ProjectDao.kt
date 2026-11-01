package com.portproof.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.portproof.core.database.model.ProjectCountsRow
import com.portproof.core.database.model.ProjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * SQL here must stay compatible with SQLite 3.18 (Android 8.0, minSdk 26): no NULLS LAST,
 * no FILTER clauses, no window functions, no UPSERT syntax.
 * Status/filter/sort literals are the stable `code` values from core:model.
 */
@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeById(id: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: String): ProjectEntity?

    @Query(
        """
        SELECT * FROM projects
        WHERE (
            (:filter = 'ALL' AND archived_at IS NULL)
            OR (:filter = 'ACTIVE' AND archived_at IS NULL AND status != 'COMPLETED')
            OR (:filter = 'COMPLETED' AND archived_at IS NULL AND status = 'COMPLETED')
            OR (:filter = 'ARCHIVED' AND archived_at IS NOT NULL)
        )
        AND (
            :pattern IS NULL
            OR name LIKE :pattern ESCAPE '\'
            OR reference LIKE :pattern ESCAPE '\'
            OR customer_name LIKE :pattern ESCAPE '\'
            OR site_name LIKE :pattern ESCAPE '\'
        )
        ORDER BY
            CASE WHEN :sort = 'NAME' THEN name END COLLATE NOCASE ASC,
            CASE WHEN :sort = 'START_DATE' THEN (start_date IS NULL) END ASC,
            CASE WHEN :sort = 'START_DATE' THEN start_date END ASC,
            updated_at DESC,
            id ASC
        """,
    )
    fun observeProjects(filter: String, sort: String, pattern: String?): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE archived_at IS NULL ORDER BY updated_at DESC, id ASC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ProjectEntity>>

    /** Open, non-archived projects whose completion date is on or before [untilEpochDay] (overdue first). */
    @Query(
        """
        SELECT * FROM projects
        WHERE archived_at IS NULL AND status != 'COMPLETED' AND end_date IS NOT NULL AND end_date <= :untilEpochDay
        ORDER BY end_date ASC, name COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    fun observeDeadlines(untilEpochDay: Long, limit: Int): Flow<List<ProjectEntity>>

    /** Global search across every project, including archived ones (listed last). */
    @Query(
        """
        SELECT * FROM projects
        WHERE name LIKE :pattern ESCAPE '\'
            OR reference LIKE :pattern ESCAPE '\'
            OR customer_name LIKE :pattern ESCAPE '\'
            OR site_name LIKE :pattern ESCAPE '\'
            OR site_address LIKE :pattern ESCAPE '\'
        ORDER BY (archived_at IS NOT NULL) ASC, updated_at DESC, id ASC
        LIMIT :limit
        """,
    )
    fun search(pattern: String, limit: Int): Flow<List<ProjectEntity>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN archived_at IS NULL THEN 1 ELSE 0 END), 0) AS total,
            COALESCE(SUM(CASE WHEN archived_at IS NULL AND status != 'COMPLETED' THEN 1 ELSE 0 END), 0) AS active,
            COALESCE(SUM(CASE WHEN archived_at IS NULL AND status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed,
            COALESCE(SUM(CASE WHEN archived_at IS NOT NULL THEN 1 ELSE 0 END), 0) AS archived,
            COALESCE(SUM(CASE WHEN archived_at IS NULL AND status = 'PLANNED' THEN 1 ELSE 0 END), 0) AS planned,
            COALESCE(SUM(CASE WHEN archived_at IS NULL AND status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) AS in_progress,
            COALESCE(SUM(CASE WHEN archived_at IS NULL AND status = 'ON_HOLD' THEN 1 ELSE 0 END), 0) AS on_hold,
            COALESCE(SUM(CASE WHEN created_at >= :since THEN 1 ELSE 0 END), 0) AS created_this_month,
            COALESCE(SUM(CASE WHEN status = 'COMPLETED' AND completed_at >= :since THEN 1 ELSE 0 END), 0)
                AS completed_this_month
        FROM projects
        """,
    )
    fun observeCounts(since: Long): Flow<ProjectCountsRow>

    /** Projects that count towards the free plan's limit: not archived and not completed. */
    @Query("SELECT COUNT(*) FROM projects WHERE archived_at IS NULL AND status != 'COMPLETED'")
    suspend fun countActive(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(project: ProjectEntity)

    @Update
    suspend fun update(project: ProjectEntity): Int

    @Query("UPDATE projects SET status = :status, completed_at = :completedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, completedAt: Long?, updatedAt: Long): Int

    @Query("UPDATE projects SET archived_at = :archivedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateArchivedAt(id: String, archivedAt: Long?, updatedAt: Long): Int

    /** Deletes the project; dependent rows are removed by ON DELETE CASCADE. */
    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
