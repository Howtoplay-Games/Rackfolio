package com.portproof.feature.subscription

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.billing.Catalog
import com.portproof.core.billing.CatalogProblem
import com.portproof.core.billing.SubscriptionState
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.AppPlan
import com.portproof.core.testing.TestSubscriptionRepository
import com.portproof.core.testing.saveScreenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders the PortProof Pro page: plans loaded, Pro active, and plans unavailable. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h1100dp-xxhdpi")
class PaywallScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val plans = listOf(TestSubscriptionRepository.sampleMonthlyPlan, TestSubscriptionRepository.sampleYearlyPlan)

    private fun render(name: String, state: PaywallUiState, dark: Boolean = false) {
        val selected = (state.subscription.catalog as? Catalog.Ready)?.plans?.lastOrNull()
        compose.setContent {
            PortProofTheme(darkTheme = dark) {
                PaywallScreen(state, selected, refreshing = false, onClose = {}, onSelect = {}, onSubscribe = {}, onRefresh = {}, onDebugOverride = {})
            }
        }
        compose.saveScreenshot(name)
    }

    @Test
    fun plans() = render("pro_plans", PaywallUiState(SubscriptionState(catalog = Catalog.Ready(plans)), activeProjects = 3))

    @Test
    fun plansDark() = render("pro_plans_dark", PaywallUiState(SubscriptionState(catalog = Catalog.Ready(plans)), activeProjects = 3), dark = true)

    @Test
    fun active() = render("pro_active", PaywallUiState(SubscriptionState(plan = AppPlan.Pro, catalog = Catalog.Ready(plans)), activeProjects = 8))

    @Test
    fun unavailableInADebugBuild() = render(
        "pro_unavailable",
        PaywallUiState(
            SubscriptionState(catalog = Catalog.Unavailable(CatalogProblem.BILLING_UNAVAILABLE), debugOverrideAvailable = true),
            activeProjects = 2,
        ),
    )
}
