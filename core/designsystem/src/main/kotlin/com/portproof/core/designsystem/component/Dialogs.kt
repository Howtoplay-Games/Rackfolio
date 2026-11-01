package com.portproof.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.portproof.core.designsystem.theme.PortProofTheme

/** Confirmation dialog. Destructive confirmations are rendered in the danger colour. */
@Composable
fun PpConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    val colors = PortProofTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = MaterialTheme.shapes.large,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (destructive) colors.dangerText else colors.accentText,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
            }
        },
    )
}
