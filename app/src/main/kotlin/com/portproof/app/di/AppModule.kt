package com.portproof.app.di

import com.portproof.app.BuildConfig
import com.portproof.core.billing.di.IsDebugBuild
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {
    /** Enables the subscription page's "treat as Pro" testing switch in debug builds only. */
    @Provides
    @IsDebugBuild
    fun provideIsDebugBuild(): Boolean = BuildConfig.DEBUG
}
