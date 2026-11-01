package com.portproof.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ActivityAction
import com.portproof.core.model.ActivityEntityType
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ActivityTrend
import com.portproof.core.model.DailyActivity
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
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

/** Renders the dashboard with representative data for design review (build/screenshots). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DashboardScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val now = Instant.now()
    private val today = LocalDate.now()

    private val state = DashboardUiState(
        isLoading = false,
        technicianName = "Sam Taylor",
        summary = ProjectSummary(
            total = 12, active = 8, completed = 4, archived = 3, planned = 2, inProgress = 5, onHold = 1,
            createdThisMonth = 3, completedThisMonth = 2,
        ),
        recentProjects = listOf(
            testProject(id = "1", name = "Warehouse CCTV upgrade", updatedAt = now.minus(2, ChronoUnit.HOURS))
                .copy(startDate = today.minusDays(6), endDate = today.plusDays(10)),
            testProject(id = "2", name = "Clinic access control", status = ProjectStatus.ON_HOLD,
                customerName = "Riverside Health", siteName = "Main clinic", updatedAt = now.minus(1, ChronoUnit.DAYS)),
            testProject(id = "3", name = "Office network refit", status = ProjectStatus.COMPLETED,
                customerName = "Carter & Webb", siteName = "Level 3", updatedAt = now.minus(3, ChronoUnit.DAYS)),
        ),
        recentActivity = listOf(
            activity(1, "Warehouse CCTV upgrade", ActivityAction.STATUS_CHANGED, ProjectStatus.IN_PROGRESS.code, 2),
            activity(2, "Clinic access control", ActivityAction.UPDATED, null, 26),
            activity(3, "Office network refit", ActivityAction.STATUS_CHANGED, ProjectStatus.COMPLETED.code, 70),
            activity(4, "School AV install", ActivityAction.CREATED, null, 96),
        ),
        trend = ActivityTrend(
            days = listOf(3, 6, 4, 8, 5, 2, 7).mapIndexed { i, c -> DailyActivity(today.minusDays(6L - i), c) },
            previousPeriodTotal = 28,
        ),
    )

    private fun activity(id: Long, project: String, action: ActivityAction, detail: String?, hoursAgo: Long) = ActivityEntry(
        id = id,
        projectId = ProjectId("p$id"),
        projectName = project,
        entityType = ActivityEntityType.PROJECT,
        entityId = "p$id",
        action = action,
        label = null,
        detail = detail,
        occurredAt = now.minus(hoursAgo, ChronoUnit.HOURS),
    )

    private val noActions = DashboardActions({}, {}, {}, {}, {})

    private fun render(dark: Boolean, name: String) {
        compose.setContent {
            PortProofTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().background(PortProofTheme.colors.background)) {
                    DashboardScreen(state, noActions)
                }
            }
        }
        compose.saveScreenshot(name)
    }

    @Test
    @Config(qualifiers = "w411dp-h1600dp-xxhdpi")
    fun dashboardLight() = render(dark = false, name = "dashboard_light")

    @Test
    @Config(qualifiers = "w411dp-h1600dp-xxhdpi")
    fun dashboardDark() = render(dark = true, name = "dashboard_dark")

    @Test
    @Config(qualifiers = "w1280dp-h900dp-mdpi")
    fun dashboardTablet() = render(dark = false, name = "dashboard_tablet")
}
