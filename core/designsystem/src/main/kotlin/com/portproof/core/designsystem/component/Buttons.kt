package com.portproof.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme

/** Near-black rounded button ("Get Started"). Light in the dark theme. */
@Composable
fun PpPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: ImageVector? = null,
) {
    val colors = PortProofTheme.colors
    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier.heightIn(min = 54.dp),
        enabled = enabled,
        shape = PortProofTheme.shapes.control,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.inverseSurface,
            contentColor = colors.onInverseSurface,
            disabledContainerColor = colors.surfaceSunken,
            disabledContentColor = colors.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = colors.onInverseSurface, strokeWidth = 2.dp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text, style = MaterialTheme.typography.labelLarge)
                if (trailingIcon != null) Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
    }
}

/** White outlined button, lower emphasis. Use [contentColor] = danger for destructive actions. */
@Composable
fun PpSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = PortProofTheme.colors.textPrimary,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        enabled = enabled,
        shape = PortProofTheme.shapes.control,
        border = BorderStroke(1.dp, PortProofTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = PortProofTheme.colors.surface, contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

enum class PpIconButtonStyle { Surface, Muted, Dark, Accent }

/** Rounded-square icon button (the reference's calendar and bell buttons). 48dp touch target. */
@Composable
fun PpIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PpIconButtonStyle = PpIconButtonStyle.Surface,
    visualSize: Dp = 44.dp,
    iconSize: Dp = 20.dp,
) {
    val colors = PortProofTheme.colors
    val (container, content) = when (style) {
        PpIconButtonStyle.Surface -> colors.surface to colors.textPrimary
        PpIconButtonStyle.Muted -> colors.surfaceMuted to colors.textPrimary
        PpIconButtonStyle.Dark -> colors.inverseSurface to colors.onInverseSurface
        PpIconButtonStyle.Accent -> colors.accent to colors.onAccent
    }
    Box(
        modifier = modifier
            .size(maxOf(48.dp, visualSize))
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(visualSize),
            shape = PortProofTheme.shapes.control,
            color = container,
            border = if (style == PpIconButtonStyle.Surface && colors.isDark) BorderStroke(1.dp, colors.border) else null,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(iconSize))
            }
        }
    }
}

/** Wide muted bar with a plus sign (the reference's add button under the gauge). */
@Composable
fun PpAddBar(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        shape = PortProofTheme.shapes.control,
        color = colors.surfaceMuted,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 14.dp)) {
            Icon(PortProofIcons.Add, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(22.dp))
        }
    }
}

/** Quiet text action used in section headers ("See all"). */
@Composable
fun PpTextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = PortProofTheme.colors.textSecondary) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 48.dp), color = Color.Transparent) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
        }
    }
}
