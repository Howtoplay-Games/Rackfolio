package com.portproof.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persisted project. Columns hold raw persisted values (codes, epoch values); conversion to
 * domain types happens in the data layer so unknown or corrupted values never crash Room.
 */
@Entity(
    tableName = "projects",
    indices = [
        Index(value = ["archived_at", "updated_at"]),
        Index(value = ["status"]),
    ],
)
data class ProjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "reference")
    val reference: String?,
    @ColumnInfo(name = "customer_name")
    val customerName: String?,
    @ColumnInfo(name = "site_name")
    val siteName: String?,
    @ColumnInfo(name = "site_address")
    val siteAddress: String?,
    @ColumnInfo(name = "description")
    val description: String?,
    /** [com.portproof.core.model.ProjectStatus.code]. */
    @ColumnInfo(name = "status")
    val status: String,
    /** Local date as epoch day. */
    @ColumnInfo(name = "start_date")
    val startDateEpochDay: Long?,
    @ColumnInfo(name = "end_date")
    val endDateEpochDay: Long?,
    /** Timestamps are epoch milliseconds (UTC). */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
    /** Null when the project is not archived. */
    @ColumnInfo(name = "archived_at")
    val archivedAt: Long?,
)
