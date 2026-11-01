package com.portproof.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectId
import com.portproof.feature.search.SearchRoute as SearchScreenRoute
import kotlinx.serialization.Serializable

/** Top-level "Search" tab (Phase 1). In Phase 2 this slot becomes Equipment and search moves into the lists. */
@Serializable
data object SearchRoute

fun NavGraphBuilder.searchScreen(onOpenProject: (ProjectId) -> Unit) {
    composable<SearchRoute> { SearchScreenRoute(onOpenProject = onOpenProject) }
}
