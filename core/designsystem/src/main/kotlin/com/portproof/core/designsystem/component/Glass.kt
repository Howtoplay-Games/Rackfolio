package com.portproof.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme
import kotlin.math.max

/** Screen background: soft vertical gradient from near-white to pale blue. */
@Composable
fun PpCanvas(modifier: Modifier = Modifier, glow: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    val colors = PortProofTheme.colors
    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(colors.canvasTop, colors.canvasBottom)))
            .then(if (glow) Modifier.glow() else Modifier),
        content = content,
    )
}

/**
 * The reference's blurred light: a warm peach core with lavender and blue halos, drawn as radial
 * gradients (no blur pass needed, so it is cheap on every API level).
 */
@Composable
fun Modifier.glow(): Modifier {
    val colors = PortProofTheme.colors
    return drawBehind {
        val r = max(size.width, size.height)
        val core = Offset(size.width * 0.52f, size.height * 0.40f)
        drawCircle(
            Brush.radialGradient(listOf(colors.orbBlue, Color.Transparent), Offset(size.width * 0.82f, size.height * 0.46f), r * 0.42f),
            radius = r * 0.42f, center = Offset(size.width * 0.82f, size.height * 0.46f),
        )
        drawCircle(
            Brush.radialGradient(listOf(colors.orbCool, Color.Transparent), Offset(size.width * 0.40f, size.height * 0.50f), r * 0.40f),
            radius = r * 0.40f, center = Offset(size.width * 0.40f, size.height * 0.50f),
        )
        drawCircle(
            Brush.radialGradient(listOf(colors.orbWarm, colors.orbWarm.copy(alpha = colors.orbWarm.alpha * 0.4f), Color.Transparent), core, r * 0.26f),
            radius = r * 0.26f, center = core,
        )
    }
}

/** Frosted glass panel: translucent white with a bright edge and a light top sheen. */
@Composable
fun PpGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PortProofTheme.colors
    val shape = PortProofTheme.shapes.tile
    val sheen = Brush.verticalGradient(listOf(colors.glassBorder.copy(alpha = 0.35f), Color.Transparent))
    val inner: @Composable () -> Unit = {
        Column(Modifier.background(sheen).padding(contentPadding), content = content)
    }
    val border = BorderStroke(1.dp, colors.glassBorder)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = colors.glass, border = border) { inner() }
    } else {
        Surface(modifier = modifier, shape = shape, color = colors.glass, border = border) { inner() }
    }
}
