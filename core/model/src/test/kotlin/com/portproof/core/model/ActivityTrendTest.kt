package com.portproof.core.model

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActivityTrendTest {

    private fun trend(counts: List<Int>, previous: Int) = ActivityTrend(
        days = counts.mapIndexed { i, c -> DailyActivity(LocalDate.of(2026, 1, 1).plusDays(i.toLong()), c) },
        previousPeriodTotal = previous,
    )

    @Test
    fun `change ratio compares with the previous period`() {
        assertEquals(0.5f, trend(listOf(3, 0, 3), previous = 4).changeRatio)
        assertEquals(-0.75f, trend(listOf(1), previous = 4).changeRatio)
    }

    @Test
    fun `change ratio is null without a baseline`() {
        assertNull(trend(listOf(2, 2), previous = 0).changeRatio)
    }

    @Test
    fun `summary completion ratio handles empty totals`() {
        assertEquals(0f, ProjectSummary.Empty.completionRatio)
        val summary = ProjectSummary(4, 3, 1, 2, 1, 1, 1, 0, 0)
        assertEquals(0.25f, summary.completionRatio)
        assertEquals(false, summary.isEmpty)
    }

    @Test
    fun `peak hours ignore empty hours and average uses the period length`() {
        val hours = MutableList(24) { 0 }.apply { this[9] = 5; this[13] = 2; this[16] = 7; this[20] = 1 }
        val insights = ActivityInsights(trend(listOf(7, 0, 7), previous = 0), hours, today = 7)
        assertEquals(setOf(16, 9, 13), insights.peakHours())
        assertEquals(setOf(16), insights.peakHours(count = 1))
        assertEquals(14f / 3, insights.dailyAverage)
        assertEquals(emptySet(), ActivityInsights(trend(listOf(0), 0), List(24) { 0 }, 0).peakHours())
    }
}
