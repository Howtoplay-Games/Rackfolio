package com.portproof.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Flat white card with a hairline border: the basic building block of every screen. */
@Composable
fun PpCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PortProofTheme.colors
    val shape = PortProofTheme.shapes.card
    val border = BorderStroke(1.dp, colors.border)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = colors.surface, border = border) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = colors.surface, border = border) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

/** Small outlined square holding an icon, as used beside card labels. */
@Composable
fun PpIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    iconSize: Dp = 16.dp,
    tint: Color = PortProofTheme.colors.textPrimary,
    containerColor: Color = PortProofTheme.colors.surface,
) {
    Surface(
        modifier = modifier.size(size),
        shape = PortProofTheme.shapes.tile,
        color = containerColor,
        border = BorderStroke(1.dp, PortProofTheme.colors.border),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
        }
    }
}

@Composable
fun PpDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = PortProofTheme.colors.divider)
}

/** Semantic colour roles for chips, deltas and status indicators. */
enum class PpTone { Neutral, Accent, Success, Warning, Danger }

data class PpToneColors(val content: Color, val container: Color, val indicator: Color)

@Composable
fun PpTone.colors(): PpToneColors {
    val c = PortProofTheme.colors
    return when (this) {
        PpTone.Neutral -> PpToneColors(c.textSecondary, c.surfaceSunken, c.textMuted)
        PpTone.Accent -> PpToneColors(c.onAccentContainer, c.accentContainer, c.accent)
        PpTone.Success -> PpToneColors(c.successText, c.successContainer, c.success)
        PpTone.Warning -> PpToneColors(c.warningText, c.warningContainer, c.warning)
        PpTone.Danger -> PpToneColors(c.dangerText, c.dangerContainer, c.danger)
    }
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
