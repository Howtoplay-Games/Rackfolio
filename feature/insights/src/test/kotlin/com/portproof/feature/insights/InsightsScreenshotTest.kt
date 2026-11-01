package com.portproof.feature.insights

import androidx.compose.runtime.Composable
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
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Renders the overview and activity log with representative data. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h1300dp-xxhdpi")
class InsightsScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.now()
    private val now = Instant.now()

    private fun entry(id: Long, name: String, action: ActivityAction, detail: String?, hoursAgo: Long) = ActivityEntry(
        id, ProjectId("p$id"), name, ActivityEntityType.PROJECT, "p$id", action, null, detail, now.minus(hoursAgo, ChronoUnit.HOURS),
    )

    private val entries = listOf(
        entry(5, "Warehouse CCTV Upgrade", ActivityAction.STATUS_CHANGED, "IN_PROGRESS", 0),
        entry(4, "Clinic Access Control", ActivityAction.UPDATED, null, 1),
        entry(3, "Office Network Refit", ActivityAction.STATUS_CHANGED, "COMPLETED", 26),
        entry(2, "School AV Install", ActivityAction.CREATED, null, 27),
        entry(1, "Depot Wi-Fi Survey", ActivityAction.ARCHIVED, null, 75),
    )

    private val overview = OverviewUiState(
        isLoading = false,
        summary = ProjectSummary(20, 14, 6, 3, 4, 9, 1, 3, 2),
        insights = ActivityInsights(
            ActivityTrend(listOf(4, 9, 6, 12, 7, 3, 8).mapIndexed { i, c -> DailyActivity(today.minusDays(6L - i), c) }, 40),
            List(24) { if (it in 8..17) it % 5 + 1 else 0 },
            today = 8,
        ),
        deadlines = listOf(
            testProject(id = "1", name = "Clinic Access Control").copy(endDate = today.minusDays(2)),
            testProject(id = "2", name = "Warehouse CCTV Upgrade").copy(endDate = today),
            testProject(id = "3", name = "School AV Install").copy(endDate = today.plusDays(5)),
        ),
        recentActivity = entries.take(4),
        today = today,
    )

    private fun render(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent { PortProofTheme(darkTheme = dark) { content() } }
        compose.saveScreenshot(name)
    }

    private val noActions = OverviewActions({}, {}, {}, {})

    @Test
    fun overviewLight() = render("overview_light") { OverviewScreen(overview, noActions) }

    @Test
    fun overviewDark() = render("overview_dark", dark = true) { OverviewScreen(overview, noActions) }

    @Test
    fun activityLog() = render("activity_log") { ActivityLogScreen(ActivityLogUiState(false, entries), {}, {}) }
}
