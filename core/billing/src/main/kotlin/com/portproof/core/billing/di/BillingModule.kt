package com.portproof.core.billing.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.portproof.core.billing.PlayBillingSubscriptionRepository
import com.portproof.core.billing.SubscriptionRepository
import com.portproof.core.data.repository.PlanRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/** Cached result of the last Google Play purchase check (files/datastore/billing.preferences_pb). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BillingStore

/** True in debug builds; enables the "treat as Pro" testing switch. Provided by the app module. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsDebugBuild

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BillingModule {

    @Binds
    abstract fun bindSubscriptionRepository(impl: PlayBillingSubscriptionRepository): SubscriptionRepository

    @Binds
    abstract fun bindPlanRepository(impl: PlayBillingSubscriptionRepository): PlanRepository

    companion object {
        @Provides
        @Singleton
        @BillingStore
        fun provideBillingDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { context.preferencesDataStoreFile("billing") },
            )
    }
}
