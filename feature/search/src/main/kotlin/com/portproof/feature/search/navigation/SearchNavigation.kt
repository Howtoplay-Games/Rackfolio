package com.portproof.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectId
import com.portproof.feature.search.SearchRoute as SearchScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute

fun NavController.navigateToSearch() = navigate(SearchRoute) { launchSingleTop = true }

fun NavGraphBuilder.searchScreen(onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit) {
    composable<SearchRoute> {
        SearchScreenRoute(onBack = onBack, onOpenProject = onOpenProject)
    }
}
