package com.portproof.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.portproof.feature.dashboard.navigation.HomeRoute

@Composable
fun rememberPortProofAppState(navController: NavHostController = rememberNavController()): PortProofAppState =
    remember(navController) { PortProofAppState(navController) }

/**
 * Navigation policy. Home is the hub (as in the reference design there is no tab bar): every other
 * screen is pushed on top of it, Back returns one step, and the close button returns to Home.
 */
@Stable
class PortProofAppState(val navController: NavHostController) {

    /**
     * Pops only when the current entry is resumed, so a double tap on a back button during a
     * transition cannot pop two screens.
     */
    fun navigateBack() {
        if (isResumed()) navController.popBackStack()
    }

    /** Leaves the current flow and returns to Home, keeping Home's state. */
    fun navigateHome() {
        if (isResumed()) navController.popBackStack(HomeRoute, inclusive = false)
    }

    private fun isResumed() = navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
}
