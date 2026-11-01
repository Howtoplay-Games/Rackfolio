package com.portproof.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectId
import com.portproof.feature.search.SearchRoute as SearchScreenRoute
import kotlinx.serialization.Serializable

/** Search tab (the orange button in the middle of the bottom bar): every project, archived included. */
@Serializable
data object SearchRoute

fun NavGraphBuilder.searchScreen(onOpenProject: (ProjectId) -> Unit) {
    composable<SearchRoute> { SearchScreenRoute(onOpenProject = onOpenProject) }
}
