package com.portproof.feature.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.feature.login.security.SecurityRoute as SecurityScreenRoute
import kotlinx.serialization.Serializable

/**
 * "Login & security" (change PIN, fingerprint unlock, lock now), opened from Settings. The welcome,
 * setup and unlock screens are not destinations: the app shows them in place of the navigation
 * graph while it is locked (see `LoginSetupRoute` and `UnlockRoute`).
 */
@Serializable
data object SecurityRoute

fun NavController.navigateToSecurity() = navigate(SecurityRoute) { launchSingleTop = true }

fun NavGraphBuilder.securityScreen(onBack: () -> Unit) {
    composable<SecurityRoute> { SecurityScreenRoute(onBack = onBack) }
}
