package com.portproof.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.R
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme

/** Screen background: a soft warm vertical gradient. */
@Composable
fun PpCanvas(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = PortProofTheme.colors
    Box(
        modifier = modifier.background(Brush.verticalGradient(listOf(colors.canvasTop, colors.canvasBottom))),
        content = content,
    )
}

/**
 * Pushed detail screens: a peach glow fading into the canvas behind square back and close buttons
 * and a large title, an optional card under the title, content below, and a bottom action row
 * pinned above the navigation bar and keyboard.
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
    Column(modifier.fillMaxSize().background(colors.background).imePadding()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(colors.headerTop, colors.headerBottom)))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(bottom = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = spacing.maxContentWidth).fillMaxWidth().padding(horizontal = spacing.screenGutter)) {
                Row(Modifier.fillMaxWidth().horizontalBleed(2.dp).padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PpIconButton(PortProofIcons.ChevronLeft, stringResource(R.string.designsystem_back), onBack)
                    Spacer(Modifier.weight(1f))
                    if (onClose != null) {
                        PpIconButton(PortProofIcons.Close, stringResource(R.string.designsystem_close_to_home), onClose)
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onHeader,
                    maxLines = 3,
                    modifier = Modifier
                        .padding(top = 14.dp, bottom = 4.dp)
                        .semantics { heading() },
                )
                if (headerCard != null) {
                    Box(Modifier.padding(top = spacing.md)) { headerCard() }
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

/** Canvas-backed screen body: warm canvas and a centred readable column. */
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
