package com.portproof.core.database.model

import androidx.room.ColumnInfo

data class ProjectCountsRow(
    @ColumnInfo(name = "total") val total: Int,
    @ColumnInfo(name = "active") val active: Int,
    @ColumnInfo(name = "completed") val completed: Int,
    @ColumnInfo(name = "archived") val archived: Int,
    @ColumnInfo(name = "planned") val planned: Int,
    @ColumnInfo(name = "in_progress") val inProgress: Int,
    @ColumnInfo(name = "on_hold") val onHold: Int,
    @ColumnInfo(name = "created_this_month") val createdThisMonth: Int,
    @ColumnInfo(name = "completed_this_month") val completedThisMonth: Int,
)
