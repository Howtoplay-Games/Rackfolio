package com.portproof.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Local activity log. Rows are removed together with their project (ON DELETE CASCADE). */
@Entity(
    tableName = "activity_log",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["project_id", "occurred_at"]),
        Index(value = ["occurred_at"]),
    ],
)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "project_id")
    val projectId: String?,
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    @ColumnInfo(name = "entity_id")
    val entityId: String?,
    @ColumnInfo(name = "action")
    val action: String,
    @ColumnInfo(name = "label")
    val label: String?,
    @ColumnInfo(name = "detail")
    val detail: String?,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,
)

/** Activity row joined with the current name of its project. */
data class ActivityWithProjectRow(
    @Embedded
    val activity: ActivityEntity,
    @ColumnInfo(name = "project_name")
    val projectName: String?,
)
