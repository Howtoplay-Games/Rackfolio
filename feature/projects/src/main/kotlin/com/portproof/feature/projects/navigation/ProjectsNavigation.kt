package com.portproof.feature.projects.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectId
import com.portproof.feature.projects.detail.ProjectDetailRoute as ProjectDetailScreenRoute
import com.portproof.feature.projects.editor.ProjectEditorRoute as ProjectEditorScreenRoute
import com.portproof.feature.projects.list.ProjectListRoute
import kotlinx.serialization.Serializable

/**
 * Projects tab. [filter] is a [com.portproof.core.model.ProjectListFilter.code] and [sort] a [com.portproof.core.model.ProjectSort.code]
 * preselected by the caller (both optional).
 */
@Serializable
data class ProjectsRoute(val filter: String? = null, val sort: String? = null)

@Serializable
data class ProjectDetailRoute(val projectId: String)

/** [projectId] null = create a new project. */
@Serializable
data class ProjectEditorRoute(val projectId: String? = null)

/** SavedStateHandle keys; they must match the route property names above. */
internal object ProjectArgs {
    const val FILTER = "filter"
    const val SORT = "sort"
    const val PROJECT_ID = "projectId"
}

fun NavController.navigateToProjectDetail(id: ProjectId, navOptions: NavOptions? = null) =
    navigate(ProjectDetailRoute(id.value), navOptions)

fun NavController.navigateToProjectEditor(id: ProjectId? = null) = navigate(ProjectEditorRoute(id?.value))

fun NavGraphBuilder.projectsScreen(
    onProjectClick: (ProjectId) -> Unit,
    onEditProject: (ProjectId) -> Unit,
    onCreateProject: () -> Unit,
) {
    composable<ProjectsRoute> {
        ProjectListRoute(onProjectClick = onProjectClick, onEditProject = onEditProject, onCreateProject = onCreateProject)
    }
}

/** [onHome] serves the header's close button: it leaves the project flow and returns to Home. */
fun NavGraphBuilder.projectDetailScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onEdit: (ProjectId) -> Unit,
    onDeleted: () -> Unit,
    onOpenPro: () -> Unit,
) {
    composable<ProjectDetailRoute> {
        ProjectDetailScreenRoute(onBack = onBack, onHome = onHome, onEdit = onEdit, onDeleted = onDeleted, onOpenPro = onOpenPro)
    }
}

/**
 * [onSaved] receives the saved project id and whether it was newly created, so the caller can
 * replace the editor with the detail screen (create) or simply go back (edit).
 */
fun NavGraphBuilder.projectEditorScreen(
    onClose: () -> Unit,
    onSaved: (id: ProjectId, created: Boolean) -> Unit,
    onOpenPro: () -> Unit,
) {
    composable<ProjectEditorRoute> {
        ProjectEditorScreenRoute(onClose = onClose, onSaved = onSaved, onOpenPro = onOpenPro)
    }
}
