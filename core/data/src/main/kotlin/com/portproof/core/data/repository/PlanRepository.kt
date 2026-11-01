package com.portproof.core.data.repository

import com.portproof.core.model.AppPlan
import kotlinx.coroutines.flow.Flow

/**
 * The user's current plan. Implemented by `core:billing` from Google Play purchases; the last known
 * plan is cached on the device so limits apply the same way offline.
 */
interface PlanRepository {
    val plan: Flow<AppPlan>

    /** The plan right now, waiting for the cached value if it has not been read yet. */
    suspend fun currentPlan(): AppPlan
}
