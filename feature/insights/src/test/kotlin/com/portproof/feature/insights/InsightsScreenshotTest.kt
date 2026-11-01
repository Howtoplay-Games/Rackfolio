package com.portproof.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ActivityAction
import com.portproof.core.model.ActivityEntityType
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ActivityInsights
import com.portproof.core.model.ActivityTrend
import com.portproof.core.model.DailyActivity
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectSummary
import com.portproof.core.testing.saveScreenshot
import com.portproof.core.testing.testProject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** Renders the Insight and Metrics Summary screens with representative data. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h1200dp-xxhdpi")
class InsightsScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.now()
    private val summary = ProjectSummary(20, 14, 6, 3, 4, 9, 1, 3, 2)
    private val insights = ActivityInsights(
        trend = ActivityTrend(
            days = listOf(4, 9, 6, 12, 7, 3, 8).mapIndexed { i, c -> DailyActivity(today.minusDays(6L - i), c) },
            previousPeriodTotal = 40,
        ),
        byHour = listOf(0, 0, 0, 0, 0, 0, 1, 3, 6, 9, 7, 8, 4, 6, 9, 11, 7, 5, 3, 2, 1, 1, 0, 0),
        today = 8,
    )
    private val insightState = InsightUiState(
        isLoading = false,
        summary = summary,
        insights = insights,
        deadlines = listOf(
            testProject(id = "1", name = "Clinic Access Control").copy(endDate = today.minusDays(2)),
            testProject(id = "2", name = "Warehouse CCTV Upgrade").copy(endDate = today.plusDays(1)),
            testProject(id = "3", name = "School AV Install").copy(endDate = today.plusDays(5)),
        ),
        today = today,
    )

    private fun render(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent {
            PortProofTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().background(PortProofTheme.colors.background)) { content() }
            }
        }
        compose.saveScreenshot(name)
    }

    @Test
    fun insightLight() = render("insight_light") { InsightScreen(insightState, {}, {}) }

    @Test
    fun insightDark() = render("insight_dark", dark = true) { InsightScreen(insightState, {}, {}) }

    @Test
    fun metricsLight() = render("metrics_light") { MetricsScreen(MetricsUiState(false, summary, insights), {}) }

    @Test
    fun metricsDark() = render("metrics_dark", dark = true) { MetricsScreen(MetricsUiState(false, summary, insights), {}) }

    @Test
    fun activityLog() = render("activity_log") {
        val now = java.time.Instant.now()
        fun entry(id: Long, name: String, action: ActivityAction, detail: String?, hoursAgo: Long) = ActivityEntry(
            id, ProjectId("p$id"), name, ActivityEntityType.PROJECT, "p$id", action, null, detail,
            now.minus(hoursAgo, java.time.temporal.ChronoUnit.HOURS),
        )
        ActivityLogScreen(
            ActivityLogUiState(
                isLoading = false,
                entries = listOf(
                    entry(5, "Warehouse CCTV Upgrade", ActivityAction.STATUS_CHANGED, "IN_PROGRESS", 0),
                    entry(4, "Clinic Access Control", ActivityAction.UPDATED, null, 1),
                    entry(3, "Office Network Refit", ActivityAction.STATUS_CHANGED, "COMPLETED", 26),
                    entry(2, "School AV Install", ActivityAction.CREATED, null, 27),
                    entry(1, "Depot Wi-Fi Survey", ActivityAction.ARCHIVED, null, 75),
                ),
            ),
            {}, {},
        )
    }
}
