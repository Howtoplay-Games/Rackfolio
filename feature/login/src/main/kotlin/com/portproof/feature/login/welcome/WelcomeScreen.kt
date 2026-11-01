package com.portproof.feature.login.welcome

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.feature.login.R
import kotlinx.coroutines.launch

private data class WelcomePage(
    val title: Int,
    val body: Int,
    val pins: List<Pair<Pair<Float, Float>, Int>>,
    val draw: DrawScope.(ArtColors) -> Unit,
)

private val pages = listOf(
    WelcomePage(
        R.string.login_welcome_1_title, R.string.login_welcome_1_body,
        listOf((0.40f to 0.25f) to R.string.login_welcome_1_pin_a, (0.645f to 0.376f) to R.string.login_welcome_1_pin_b, (0.54f to 0.60f) to R.string.login_welcome_1_pin_c),
    ) { drawRack(it) },
    WelcomePage(
        R.string.login_welcome_2_title, R.string.login_welcome_2_body,
        listOf((0.667f to 0.554f) to R.string.login_welcome_2_pin_a, (0.332f to 0.657f) to R.string.login_welcome_2_pin_b, (0.751f to 0.657f) to R.string.login_welcome_2_pin_c),
    ) { drawSchedule(it) },
    WelcomePage(
        R.string.login_welcome_3_title, R.string.login_welcome_3_body,
        listOf((0.5f to 0.317f) to R.string.login_welcome_3_pin_a, (0.397f to 0.5f) to R.string.login_welcome_3_pin_b, (0.574f to 0.73f) to R.string.login_welcome_3_pin_c),
    ) { drawPhone(it) },
)

/**
 * First-run welcome, laid out as the reference's middle screen: artwork with pinned labels fading
 * into the canvas, a large two-line title, a short line of text, page dots and a dark button.
 */
@Composable
internal fun WelcomeScreen(onGetStarted: () -> Unit, modifier: Modifier = Modifier, initialPage: Int = 0) {
    val colors = PortProofTheme.colors
    val pagerState = rememberPagerState(initialPage = initialPage) { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex
    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.navigationBars),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(pagerState, Modifier.weight(1f)) { index ->
            val page = pages[index]
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Brush.verticalGradient(listOf(colors.headerTop, colors.background)))
                        .windowInsetsPadding(WindowInsets.statusBars),
                    // As in the reference, the picture sits right above the headline; the glow fills the top.
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    PinnedArt(
                        description = stringResource(R.string.login_welcome_art),
                        pins = page.pins.map { (point, text) -> ArtPin(point.first, point.second, stringResource(text)) },
                        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(1.2f),
                        draw = page.draw,
                    )
                }
                Text(
                    stringResource(page.title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 20.dp).semantics { heading() },
                )
                Text(
                    stringResource(page.body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 420.dp).padding(horizontal = 32.dp, vertical = 12.dp),
                )
            }
        }
        PageDots(pagerState.currentPage, pages.size)
        PpPrimaryButton(
            text = stringResource(if (isLast) R.string.login_get_started else R.string.login_next),
            onClick = {
                if (isLast) onGetStarted() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun PageDots(current: Int, count: Int) {
    val colors = PortProofTheme.colors
    val description = stringResource(R.string.login_page_of, current + 1, count)
    Row(
        Modifier
            .padding(top = 8.dp)
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(count) { i ->
            val width by animateDpAsState(if (i == current) 18.dp else 6.dp, label = "dot")
            Box(
                Modifier
                    .height(6.dp)
                    .size(width = width, height = 6.dp)
                    .background(if (i == current) colors.textPrimary else colors.surfaceSunken, PortProofTheme.shapes.pill),
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}
