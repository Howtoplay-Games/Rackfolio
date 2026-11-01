package com.portproof.feature.projects

import com.portproof.core.model.scheduleStateOf
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ActivityAction
import com.portproof.core.model.ActivityEntityType
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ProjectSummary
import com.portproof.core.testing.saveScreenshot
import com.portproof.core.testing.testProject
import com.portproof.feature.projects.detail.DetailActions
import com.portproof.feature.projects.detail.ProjectDetailScreen
import com.portproof.feature.projects.detail.ProjectDetailUiState
import com.portproof.feature.projects.detail.reviewOf
import com.portproof.feature.projects.list.ProjectListScreen
import com.portproof.feature.projects.list.ProjectListUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Renders the project detail (with highlighted findings) and the project list. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h1100dp-xxhdpi")
class ProjectScreensScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.now()
    private val project = testProject(name = "Warehouse CCTV Upgrade", siteName = null)
        .copy(siteAddress = null, startDate = today.minusDays(20), endDate = today.minusDays(2), description = "Replace 24 analogue cameras with IP cameras, new PoE switch in the comms room, NVR with 30 days of retention.")
    private val detail = ProjectDetailUiState.Success(
        project = project,
        activity = listOf(
            ActivityEntry(2, project.id, project.name, ActivityEntityType.PROJECT, project.id.value, ActivityAction.STATUS_CHANGED, null, "IN_PROGRESS", Instant.now().minus(3, ChronoUnit.HOURS)),
            ActivityEntry(1, project.id, project.name, ActivityEntityType.PROJECT, project.id.value, ActivityAction.CREATED, null, null, Instant.now().minus(21, ChronoUnit.DAYS)),
        ),
        schedule = scheduleStateOf(project, today),
        review = reviewOf(project, today),
        today = today,
    )
    private val noDetailActions = DetailActions({}, {}, {}, {}, {}, {}, {})

    private fun render(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent { PortProofTheme(darkTheme = dark) { content() } }
        compose.saveScreenshot(name)
    }

    @Test
    fun detailLight() = render("project_detail") { ProjectDetailScreen(detail, SnackbarHostState(), noDetailActions) }

    @Test
    fun detailDark() = render("project_detail_dark", dark = true) { ProjectDetailScreen(detail, SnackbarHostState(), noDetailActions) }

    @Test
    fun list() = render("project_list") {
        ProjectListScreen(
            state = ProjectListUiState(
                isLoading = false,
                filter = ProjectListFilter.ALL,
                sort = ProjectSort.RECENTLY_UPDATED,
                projects = listOf(
                    project,
                    testProject(id = "2", name = "Clinic Access Control", status = ProjectStatus.IN_PROGRESS, customerName = "Riverside Health", siteName = "Main clinic")
                        .copy(startDate = today.minusDays(5), endDate = today.plusDays(9), updatedAt = Instant.now().minus(2, ChronoUnit.HOURS)),
                    testProject(id = "3", name = "Office Network Refit", status = ProjectStatus.COMPLETED, customerName = "Carter & Webb", siteName = "Level 3")
                        .copy(startDate = today.minusDays(40), endDate = today.minusDays(12), updatedAt = Instant.now().minus(3, ChronoUnit.DAYS)),
                ),
                summary = ProjectSummary(20, 14, 6, 3, 4, 9, 1, 3, 2),
                today = today,
            ),
            query = "",
            onQueryChange = {},
            onFilterSelected = {},
            onSortSelected = {},
            onProjectClick = { _: ProjectId -> },
            onEditProject = { _: ProjectId -> },
            onCreateProject = {},
        )
    }
}
