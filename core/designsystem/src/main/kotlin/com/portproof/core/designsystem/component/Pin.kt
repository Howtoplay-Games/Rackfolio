package com.portproof.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme

/** Row of PIN digit dots. [description] (e.g. "3 of 6 digits entered") is read by screen readers. */
@Composable
fun PpPinDots(length: Int, filled: Int, description: String, modifier: Modifier = Modifier, error: Boolean = false) {
    val colors = PortProofTheme.colors
    val ink = if (error) colors.dangerText else colors.textPrimary
    Row(
        modifier.clearAndSetSemantics {
            contentDescription = description
            liveRegion = LiveRegionMode.Polite
        },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        repeat(length) { i ->
            Box(
                Modifier
                    .size(14.dp)
                    .then(
                        if (i < filled) {
                            Modifier.background(ink, PortProofTheme.shapes.pill)
                        } else {
                            Modifier.border(1.5.dp, if (error) colors.dangerText else colors.textMuted, PortProofTheme.shapes.pill)
                        },
                    ),
            )
        }
    }
}

/**
 * Number pad for PIN entry: 1–9, then [leftKey] (for example fingerprint unlock), 0 and backspace.
 * Keys are 64dp tall white tiles.
 */
@Composable
fun PpKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    backspaceLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leftKey: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(modifier.widthIn(max = 360.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf("123", "456", "789").forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { digit -> DigitKey(digit, enabled) { onDigit(digit) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (leftKey != null) leftKey() else Box(Modifier.weight(1f))
            DigitKey('0', enabled) { onDigit('0') }
            PpKeypadIconKey(PortProofIcons.Backspace, backspaceLabel, enabled, onBackspace)
        }
    }
}

@Composable
private fun RowScope.DigitKey(digit: Char, enabled: Boolean, onClick: () -> Unit) {
    val colors = PortProofTheme.colors
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f).height(60.dp),
        shape = PortProofTheme.shapes.tile,
        color = colors.surface,
        border = if (colors.isDark) BorderStroke(1.dp, colors.border) else null,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                digit.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) colors.textPrimary else colors.textMuted,
            )
        }
    }
}

/** Transparent keypad key holding an icon (backspace, fingerprint). */
@Composable
fun RowScope.PpKeypadIconKey(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .weight(1f)
            .height(60.dp)
            .semantics {
                contentDescription = label
                role = Role.Button
            },
        shape = PortProofTheme.shapes.tile,
        color = Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = PortProofTheme.colors.textPrimary, modifier = Modifier.size(26.dp))
        }
    }
}

/** Selectable card with a round check on the left (plan choices). */
@Composable
fun PpChoiceCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = PortProofTheme.colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = PortProofTheme.shapes.card,
        color = colors.surface,
        border = if (selected) BorderStroke(2.dp, colors.accent) else BorderStroke(1.dp, colors.border),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .then(
                        if (selected) {
                            Modifier.background(colors.inverseSurface, PortProofTheme.shapes.pill)
                        } else {
                            Modifier.border(1.5.dp, colors.textMuted, PortProofTheme.shapes.pill)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Icon(PortProofIcons.Check, null, tint = colors.onInverseSurface, modifier = Modifier.size(14.dp))
            }
            content()
        }
    }
}
