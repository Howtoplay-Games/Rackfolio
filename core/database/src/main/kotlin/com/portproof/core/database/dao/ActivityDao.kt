package com.portproof.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.portproof.core.database.model.ActivityEntity
import com.portproof.core.database.model.ActivityWithProjectRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert
    suspend fun insert(activity: ActivityEntity): Long

    @Query(
        """
        SELECT activity_log.*, projects.name AS project_name
        FROM activity_log
        LEFT JOIN projects ON projects.id = activity_log.project_id
        ORDER BY activity_log.occurred_at DESC, activity_log.id DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<ActivityWithProjectRow>>

    @Query(
        """
        SELECT activity_log.*, projects.name AS project_name
        FROM activity_log
        LEFT JOIN projects ON projects.id = activity_log.project_id
        WHERE activity_log.project_id = :projectId
        ORDER BY activity_log.occurred_at DESC, activity_log.id DESC
        LIMIT :limit
        """,
    )
    fun observeForProject(projectId: String, limit: Int): Flow<List<ActivityWithProjectRow>>

    /** Timestamps only; day bucketing happens in Kotlin so time zones are handled correctly. */
    @Query("SELECT occurred_at FROM activity_log WHERE occurred_at >= :since ORDER BY occurred_at ASC")
    fun observeTimestampsSince(since: Long): Flow<List<Long>>
}
