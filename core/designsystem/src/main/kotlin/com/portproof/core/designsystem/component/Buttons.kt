package com.portproof.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Main call to action. Shows a spinner and ignores taps while [loading]. */
@Composable
fun PpPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val colors = PortProofTheme.colors
    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier.heightIn(min = 52.dp),
        enabled = enabled,
        shape = PortProofTheme.shapes.control,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = colors.onAccent,
            disabledContainerColor = colors.surfaceSunken,
            disabledContentColor = colors.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = colors.onAccent, strokeWidth = 2.dp)
        } else {
            ButtonContent(text, leadingIcon)
        }
    }
}

/** Outlined, lower-emphasis action. Use [contentColor] = danger for destructive actions. */
@Composable
fun PpSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentColor: Color = PortProofTheme.colors.textPrimary,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = PortProofTheme.shapes.control,
        border = BorderStroke(1.dp, PortProofTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = PortProofTheme.colors.surface,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ButtonContent(text, leadingIcon)
    }
}

@Composable
private fun ButtonContent(text: String, leadingIcon: ImageVector?) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (leadingIcon != null) Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Compact outlined action used in card headers ("+ New", "View all"). 48dp touch target. */
@Composable
fun PpChipButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val colors = PortProofTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = PortProofTheme.shapes.control,
        color = Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                shape = PortProofTheme.shapes.control,
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border),
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 32.dp)
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, null, tint = colors.textPrimary, modifier = Modifier.size(14.dp))
                    }
                    Text(text, style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                }
            }
        }
    }
}

enum class PpIconButtonShape { Square, Circle }

/** Outlined icon button used in screen headers. Visual size 40dp inside a 48dp touch target. */
@Composable
fun PpIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: PpIconButtonShape = PpIconButtonShape.Square,
    visualSize: Dp = 40.dp,
    filled: Boolean = false,
) {
    val colors = PortProofTheme.colors
    val surfaceShape: Shape = when (shape) {
        PpIconButtonShape.Square -> PortProofTheme.shapes.control
        PpIconButtonShape.Circle -> PortProofTheme.shapes.pill
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(visualSize),
            shape = surfaceShape,
            color = if (filled) colors.inverseSurface else colors.surface,
            border = if (filled) null else BorderStroke(1.dp, colors.border),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (filled) colors.onInverseSurface else colors.textPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
