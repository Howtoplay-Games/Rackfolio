package com.portproof.core.data.repository

import com.portproof.core.data.mapper.asExternalModelOrNull
import com.portproof.core.database.dao.ActivityDao
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ActivityInsights
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

    override fun observeActivityTrend(days: Int): Flow<ActivityTrend> = observeActivityInsights(days).map { it.trend }

    override fun observeActivityInsights(days: Int): Flow<ActivityInsights> {
        require(days > 0) { "days must be positive" }
        val zone = clock.zone
        val today = LocalDate.now(clock)
        val firstDay = today.minusDays(2L * days - 1)
        val since = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
        val currentPeriodStart = today.minusDays(days - 1L)

        return activityDao.observeTimestampsSince(since).map { timestamps ->
            val times = timestamps.map { Instant.ofEpochMilli(it).atZone(zone) }
            val perDay = times.groupingBy { it.toLocalDate() }.eachCount()
            val current = (days - 1 downTo 0).map { offset ->
                val date = today.minusDays(offset.toLong())
                DailyActivity(date, perDay[date] ?: 0)
            }
            val previousTotal = (2 * days - 1 downTo days).sumOf { offset -> perDay[today.minusDays(offset.toLong())] ?: 0 }
            val byHour = IntArray(24)
            times.filter { !it.toLocalDate().isBefore(currentPeriodStart) }.forEach { byHour[it.hour]++ }
            ActivityInsights(
                trend = ActivityTrend(days = current, previousPeriodTotal = previousTotal),
                byHour = byHour.toList(),
                today = perDay[today] ?: 0,
            )
        }
    }
}
