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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Main call to action: black pill (white in the dark theme). Ignores taps while [loading]. */
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
        modifier = modifier.heightIn(min = 54.dp),
        enabled = enabled,
        shape = PortProofTheme.shapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.inverseSurface,
            contentColor = colors.onInverseSurface,
            disabledContainerColor = colors.surfaceSunken,
            disabledContentColor = colors.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = colors.onInverseSurface, strokeWidth = 2.dp)
        } else {
            ButtonContent(text, leadingIcon)
        }
    }
}

/** Outlined pill. Use [contentColor] = danger for destructive actions. */
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
        shape = PortProofTheme.shapes.pill,
        border = BorderStroke(1.dp, PortProofTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = PortProofTheme.colors.surface, contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    ) {
        ButtonContent(text, leadingIcon)
    }
}

/** Small black pill inside cards ("Open Project"), as the reference's "Start Now". 48dp touch target. */
@Composable
fun PpPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    Box(modifier.heightIn(min = 48.dp), contentAlignment = Alignment.CenterStart) {
        Surface(onClick = onClick, shape = PortProofTheme.shapes.pill, color = colors.inverseSurface) {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onInverseSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

/** Grey text action used in section headers ("See Details"). */
@Composable
fun PpTextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 48.dp), color = Color.Transparent) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = PortProofTheme.colors.textSecondary)
        }
    }
}

@Composable
private fun ButtonContent(text: String, leadingIcon: ImageVector?) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (leadingIcon != null) Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Soft pill action ("+ New"). 48dp touch target. */
@Composable
fun PpChipButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val colors = PortProofTheme.colors
    Box(modifier.heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
        Surface(onClick = onClick, shape = PortProofTheme.shapes.pill, color = colors.surfaceMuted) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) Icon(leadingIcon, null, tint = colors.textPrimary, modifier = Modifier.size(15.dp))
                Text(text, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
            }
        }
    }
}

enum class PpIconButtonStyle { Soft, Outlined, Filled }

/** Circular icon button (header bell, back arrow). Visual size 44dp inside a 48dp touch target. */
@Composable
fun PpIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PpIconButtonStyle = PpIconButtonStyle.Soft,
    visualSize: Dp = 44.dp,
) {
    val colors = PortProofTheme.colors
    val container = when (style) {
        PpIconButtonStyle.Soft -> colors.surfaceMuted
        PpIconButtonStyle.Outlined -> colors.surface
        PpIconButtonStyle.Filled -> colors.inverseSurface
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
            shape = PortProofTheme.shapes.pill,
            color = container,
            border = if (style == PpIconButtonStyle.Outlined) BorderStroke(1.dp, colors.border) else null,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (style == PpIconButtonStyle.Filled) colors.onInverseSurface else colors.textPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
