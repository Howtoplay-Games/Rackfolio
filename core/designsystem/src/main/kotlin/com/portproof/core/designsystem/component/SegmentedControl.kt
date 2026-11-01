package com.portproof.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Single-choice control on a sunken track; the selected segment is a raised white surface. */
@Composable
fun <T> PpSegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = PortProofTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceMuted, PortProofTheme.shapes.control)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val container by animateColorAsState(if (isSelected) colors.inverseSurface else Color.Transparent, label = "segment")
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .selectable(isSelected, enabled = enabled, role = Role.RadioButton) { onSelect(option) },
                shape = RoundedCornerShape(11.dp),
                color = container,
                            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) colors.onInverseSurface else colors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
