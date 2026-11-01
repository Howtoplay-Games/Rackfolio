package com.portproof.core.data.repository

import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ActivityTrend
import com.portproof.core.model.ProjectId
import kotlinx.coroutines.flow.Flow

/** Read access to the activity log. Entries are written by the repositories that change data. */
interface ActivityRepository {
    fun observeRecentActivity(limit: Int): Flow<List<ActivityEntry>>
    fun observeProjectActivity(projectId: ProjectId, limit: Int): Flow<List<ActivityEntry>>

    /** Daily counts for the last [days] days (today included) plus the total of the [days] before. */
    fun observeActivityTrend(days: Int): Flow<ActivityTrend>
}
