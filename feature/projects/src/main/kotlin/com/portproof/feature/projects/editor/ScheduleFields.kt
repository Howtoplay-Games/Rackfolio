package com.portproof.feature.projects.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpPickerField
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.ui.formatDate
import com.portproof.feature.projects.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import com.portproof.core.ui.R as UiR

private enum class DateTarget { Start, End }

@Composable
internal fun ScheduleFields(form: ProjectForm, endDateError: String?, callbacks: EditorCallbacks) {
    var picking by rememberSaveable { mutableStateOf<DateTarget?>(null) }
    PpCard {
        PpSectionHeader(stringResource(R.string.project_section_schedule), style = MaterialTheme.typography.titleSmall)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DateField(stringResource(R.string.project_field_start), form.startDate, null,
                onPick = { picking = DateTarget.Start }, onClear = { callbacks.onStartDate(null) })
            DateField(stringResource(R.string.project_field_end), form.endDate, endDateError,
                onPick = { picking = DateTarget.End }, onClear = { callbacks.onEndDate(null) })
        }
    }
    when (picking) {
        DateTarget.Start -> PortProofDatePicker(form.startDate ?: form.endDate, { callbacks.onStartDate(it) }) { picking = null }
        DateTarget.End -> PortProofDatePicker(form.endDate ?: form.startDate, { callbacks.onEndDate(it) }) { picking = null }
        null -> Unit
    }
}

@Composable
private fun DateField(label: String, date: LocalDate?, error: String?, onPick: () -> Unit, onClear: () -> Unit) {
    PpPickerField(
        value = date?.let { formatDate(it) }.orEmpty(),
        label = label,
        onClick = onPick,
        placeholder = stringResource(R.string.project_field_pick_date),
        error = error,
        icon = PortProofIcons.Calendar,
        trailingAction = if (date != null) {
            {
                IconButton(onClick = onClear) {
                    Icon(PortProofIcons.Close, stringResource(R.string.project_field_clear_date), Modifier.size(18.dp))
                }
            }
        } else {
            null
        },
    )
}

/** Material 3 date picker. It works in UTC milliseconds, so conversion uses UTC on both sides. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortProofDatePicker(initial: LocalDate?, onSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
    )
    val colors = PortProofTheme.colors
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    onDismiss()
                },
            ) { Text(stringResource(R.string.project_date_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(UiR.string.ui_action_cancel)) } },
        colors = DatePickerDefaults.colors(containerColor = colors.surface),
    ) {
        DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = colors.surface))
    }
}
