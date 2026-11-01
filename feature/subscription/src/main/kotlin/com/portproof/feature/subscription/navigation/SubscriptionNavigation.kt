package com.portproof.feature.subscription.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.feature.subscription.PaywallRoute
import kotlinx.serialization.Serializable

/** PortProof Pro: plans, purchase through Google Play, and the current subscription. */
@Serializable
data object ProRoute

fun NavController.navigateToPro() = navigate(ProRoute) { launchSingleTop = true }

fun NavGraphBuilder.proScreen(onClose: () -> Unit) {
    composable<ProRoute> { PaywallRoute(onClose = onClose) }
}
