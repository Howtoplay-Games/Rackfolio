package com.portproof.core.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

@Composable
internal fun ppTextFieldColors(): TextFieldColors {
    val c = PortProofTheme.colors
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = c.surfaceMuted,
        unfocusedContainerColor = c.surfaceMuted,
        disabledContainerColor = c.surfaceMuted,
        errorContainerColor = c.surface,
        focusedBorderColor = c.accentStrong,
        unfocusedBorderColor = c.surfaceMuted,
        errorBorderColor = c.dangerText,
        focusedLabelColor = c.accentStrong,
        unfocusedLabelColor = c.textSecondary,
        cursorColor = c.accentStrong,
        focusedTextColor = c.textPrimary,
        unfocusedTextColor = c.textPrimary,
        disabledTextColor = c.textPrimary,
        disabledBorderColor = c.border,
        disabledLabelColor = c.textSecondary,
        focusedPlaceholderColor = c.textMuted,
        unfocusedPlaceholderColor = c.textMuted,
        disabledTrailingIconColor = c.textSecondary,
        unfocusedTrailingIconColor = c.textSecondary,
    )
}

/**
 * Labelled text input. [error] replaces [supportingText] and is announced by accessibility
 * services. Input is never truncated silently; length limits are reported as errors instead.
 */
@Composable
fun PpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (error != null) error(error) },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = (error ?: supportingText)?.let { { Text(it) } },
        isError = error != null,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        shape = PortProofTheme.shapes.field,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = ppTextFieldColors(),
    )
}

/** Read-only field that opens a picker (e.g. a date picker) when tapped. */
@Composable
fun PpPickerField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    icon: ImageVector? = null,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { if (it is PressInteraction.Release) onClick() }
    }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                if (error != null) error(error)
            },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = error?.let { { Text(it) } },
        isError = error != null,
        singleLine = true,
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) } },
        trailingIcon = trailingAction,
        shape = PortProofTheme.shapes.field,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = ppTextFieldColors(),
        interactionSource = interactionSource,
    )
}
