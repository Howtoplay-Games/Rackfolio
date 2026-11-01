package com.portproof.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.portproof.core.data.repository.ActivityRepository
import com.portproof.core.data.repository.AppLockRepository
import com.portproof.core.data.repository.DataStoreAppLockRepository
import com.portproof.core.data.repository.DataStoreUserPreferencesRepository
import com.portproof.core.data.repository.OfflineFirstActivityRepository
import com.portproof.core.data.repository.OfflineFirstProjectRepository
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.data.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton

/** The app-lock preferences file (PIN hash, attempt counter), kept apart from user preferences. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppLockStore

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: OfflineFirstProjectRepository): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: OfflineFirstActivityRepository): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: DataStoreUserPreferencesRepository): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAppLockRepository(impl: DataStoreAppLockRepository): AppLockRepository

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()

        /** Stored in app-private files/datastore/. A corrupted file is replaced with defaults. */
        @Provides
        @Singleton
        fun provideUserPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { context.preferencesDataStoreFile("user_preferences") },
            )

        /**
         * files/datastore/app_lock.preferences_pb, excluded from cloud backup and device transfer
         * (see the backup rules). A corrupted file falls back to "no PIN", which asks for a new one.
         */
        @Provides
        @Singleton
        @AppLockStore
        fun provideAppLockDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { context.preferencesDataStoreFile("app_lock") },
            )
    }
}
