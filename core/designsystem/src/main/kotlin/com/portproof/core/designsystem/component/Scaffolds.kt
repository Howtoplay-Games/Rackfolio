package com.portproof.core.designsystem.component

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.portproof.core.designsystem.R
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme

/** While composed, shows light (white) status-bar icons, for screens with a coloured header. */
@Composable
fun LightStatusBarIcons() {
    if (LocalInspectionMode.current) return
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}

/**
 * Reference "Scan & Review text" layout: periwinkle gradient header with back and close buttons, a
 * large white title and an optional card that overlaps the header, content on the canvas below,
 * and a bottom action bar pinned above the navigation bar and keyboard.
 */
@Composable
fun PpGradientScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    headerCard: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PortProofTheme.colors
    val spacing = PortProofTheme.spacing
    LightStatusBarIcons()
    Column(modifier.fillMaxSize().background(colors.background).imePadding()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(colors.headerTop, colors.headerMiddle, colors.headerBottom)), PortProofTheme.shapes.header)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(bottom = spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = spacing.maxContentWidth).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    PpCircleButton(PortProofIcons.ChevronLeft, stringResource(R.string.designsystem_back), onBack, style = PpCircleStyle.OnHeader)
                    Spacer(Modifier.weight(1f))
                    if (onClose != null) {
                        PpCircleButton(PortProofIcons.Close, stringResource(R.string.designsystem_close_to_home), onClose, style = PpCircleStyle.OnHeader)
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onHeader,
                    maxLines = 3,
                    modifier = Modifier
                        .padding(horizontal = spacing.screenGutter + 4.dp, vertical = 4.dp)
                        .semantics { heading() },
                )
                if (headerCard != null) {
                    Box(Modifier.padding(start = spacing.screenGutter, end = spacing.screenGutter, top = spacing.md)) { headerCard() }
                }
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = spacing.maxContentWidth).fillMaxWidth().weight(1f), content = content)
        }
        if (bottomBar != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = spacing.screenGutter, vertical = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.md, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = bottomBar,
            )
        }
    }
}

/**
 * List-screen header from the reference's third screen: circular back button and actions on one
 * row, then a large two-line title on the canvas.
 */
@Composable
fun PpScreenHeader(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = PortProofTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 4.dp, bottom = 8.dp),
    ) {
        Row(Modifier.fillMaxWidth().horizontalBleed(4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                PpCircleButton(PortProofIcons.ChevronLeft, stringResource(R.string.designsystem_back), onBack, style = PpCircleStyle.Glass)
            }
            Spacer(Modifier.weight(1f))
            actions()
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary, modifier = Modifier.semantics { heading() })
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** Canvas-backed screen body for list screens: gradient canvas, centred readable column. */
@Composable
fun PpListScreen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    PpCanvas(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                Modifier.widthIn(max = PortProofTheme.spacing.maxContentWidth).fillMaxWidth().weight(1f),
                content = content,
            )
        }
    }
}
