package com.portproof.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/**
 * White card with 20dp corners on the warm canvas. [glow] adds the reference meal cards' faint peach
 * wash at the bottom edge.
 */
@Composable
fun PpCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    glow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PortProofTheme.colors
    val shape = PortProofTheme.shapes.card
    val border = if (colors.isDark) BorderStroke(1.dp, colors.border) else null
    val inner = if (glow) {
        Modifier.background(Brush.verticalGradient(0.55f to colors.surface, 1f to colors.cardGlow))
    } else {
        Modifier
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = colors.surface, border = border) {
            Column(inner.padding(contentPadding), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = colors.surface, border = border) {
            Column(inner.padding(contentPadding), content = content)
        }
    }
}

/** Inner grey panel inside a card (chart areas, summaries). */
@Composable
fun PpPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PortProofTheme.colors.surfaceMuted, PortProofTheme.shapes.panel)
            .padding(14.dp),
        content = content,
    )
}

/** Soft circle holding an outline icon (list rows, activity entries, empty states). */
@Composable
fun PpIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    tint: Color = PortProofTheme.colors.textPrimary,
    containerColor: Color = PortProofTheme.colors.surfaceMuted,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, PortProofTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun PpDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = PortProofTheme.colors.divider)
}

/**
 * Lets a row extend [amount] beyond its parent's horizontal padding on both sides. Used for header
 * rows of 48dp icon buttons, whose 40dp visuals would otherwise sit 4dp inside the content gutter.
 */
fun Modifier.horizontalBleed(amount: Dp): Modifier = layout { measurable, constraints ->
    if (!constraints.hasBoundedWidth) {
        val placeable = measurable.measure(constraints)
        return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    val bleed = amount.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(minWidth = constraints.minWidth + bleed * 2, maxWidth = constraints.maxWidth + bleed * 2),
    )
    layout((placeable.width - bleed * 2).coerceAtLeast(0), placeable.height) { placeable.place(-bleed, 0) }
}
