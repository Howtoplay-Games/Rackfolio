package com.portproof.core.database.di

import android.content.Context
import androidx.room.Room
import com.portproof.core.database.PortProofDatabase
import com.portproof.core.database.dao.ActivityDao
import com.portproof.core.database.dao.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    /** Stored in app-private storage (`/data/data/<package>/databases`). WAL mode is Room's default. */
    @Provides
    @Singleton
    fun providePortProofDatabase(@ApplicationContext context: Context): PortProofDatabase =
        Room.databaseBuilder(context, PortProofDatabase::class.java, PortProofDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun provideProjectDao(database: PortProofDatabase): ProjectDao = database.projectDao()

    @Provides
    fun provideActivityDao(database: PortProofDatabase): ActivityDao = database.activityDao()
}
