package com.portproof.core.testing

import com.portproof.core.data.repository.ActivityRepository
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ActivityTrend
import com.portproof.core.model.DailyActivity
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ThemeMode
import com.portproof.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class TestActivityRepository : ActivityRepository {
    val entries = MutableStateFlow<List<ActivityEntry>>(emptyList())
    val trend = MutableStateFlow(
        ActivityTrend(
            days = (6 downTo 0).map { DailyActivity(LocalDate.of(2026, 11, 1).minusDays(it.toLong()), 0) },
            previousPeriodTotal = 0,
        ),
    )

    override fun observeRecentActivity(limit: Int): Flow<List<ActivityEntry>> = entries.map { it.take(limit) }

    override fun observeProjectActivity(projectId: ProjectId, limit: Int): Flow<List<ActivityEntry>> =
        entries.map { all -> all.filter { it.projectId == projectId }.take(limit) }

    override fun observeActivityTrend(days: Int): Flow<ActivityTrend> = trend
}

class TestUserPreferencesRepository(initial: UserPreferences = UserPreferences()) : UserPreferencesRepository {
    private val state = MutableStateFlow(initial)
    override val preferences: Flow<UserPreferences> = state

    override suspend fun setTechnicianName(name: String) = state.update { it.copy(technicianName = name) }
    override suspend fun setCompanyName(name: String) = state.update { it.copy(companyName = name) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
}
