package com.portproof.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.portproof.core.database.dao.ActivityDao
import com.portproof.core.database.dao.ProjectDao
import com.portproof.core.database.model.ActivityEntity
import com.portproof.core.database.model.ProjectEntity

/**
 * The single source of truth for all PortProof data.
 *
 * Schema changes must bump [version] and ship a migration (prefer Room auto-migrations).
 * Destructive migration fallbacks are never used: losing on-site records is unacceptable.
 * See DATABASE_SCHEMA.md for the full, phase-by-phase schema plan.
 */
@Database(
    entities = [
        ProjectEntity::class,
        ActivityEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PortProofDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun activityDao(): ActivityDao

    companion object {
        const val DATABASE_NAME = "portproof.db"
    }
}
