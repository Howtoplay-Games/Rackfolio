package com.portproof.core.data.repository

import com.portproof.core.data.mapper.asExternalModelOrNull
import com.portproof.core.database.dao.ActivityDao
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ActivityTrend
import com.portproof.core.model.DailyActivity
import com.portproof.core.model.ProjectId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

internal class OfflineFirstActivityRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val clock: Clock,
) : ActivityRepository {

    override fun observeRecentActivity(limit: Int): Flow<List<ActivityEntry>> =
        activityDao.observeRecent(limit).map { rows -> rows.mapNotNull { it.asExternalModelOrNull() } }

    override fun observeProjectActivity(projectId: ProjectId, limit: Int): Flow<List<ActivityEntry>> =
        activityDao.observeForProject(projectId.value, limit)
            .map { rows -> rows.mapNotNull { it.asExternalModelOrNull() } }

    override fun observeActivityTrend(days: Int): Flow<ActivityTrend> {
        require(days > 0) { "days must be positive" }
        val zone = clock.zone
        val today = LocalDate.now(clock)
        val firstDay = today.minusDays(2L * days - 1)
        val since = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()

        return activityDao.observeTimestampsSince(since).map { timestamps ->
            val perDay = timestamps.groupingBy { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.eachCount()
            val current = (days - 1 downTo 0).map { offset ->
                val date = today.minusDays(offset.toLong())
                DailyActivity(date, perDay[date] ?: 0)
            }
            val previousTotal = (2 * days - 1 downTo days).sumOf { offset ->
                perDay[today.minusDays(offset.toLong())] ?: 0
            }
            ActivityTrend(days = current, previousPeriodTotal = previousTotal)
        }
    }
}
