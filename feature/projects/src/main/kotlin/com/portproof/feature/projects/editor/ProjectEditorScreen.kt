package com.portproof.feature.projects.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import com.portproof.core.designsystem.component.PpSecondaryButton
import com.portproof.core.designsystem.component.PpIconTile
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpConfirmDialog
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpGradientScreen
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.component.PpSegmentedControl
import com.portproof.core.designsystem.component.PpTextField
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.FieldError
import com.portproof.core.model.ProjectField
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.core.ui.labelRes
import com.portproof.feature.projects.R
import com.portproof.core.ui.R as UiR

@Composable
internal fun ProjectEditorRoute(
    onClose: () -> Unit,
    onSaved: (ProjectId, Boolean) -> Unit,
    onOpenPro: () -> Unit,
    viewModel: ProjectEditorViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    val requestClose = { if (viewModel.hasUnsavedChanges) confirmDiscard = true else onClose() }

    BackHandler(enabled = viewModel.hasUnsavedChanges) { confirmDiscard = true }

    val savedId = viewModel.savedProjectId
    LaunchedEffect(savedId) {
        if (savedId != null) {
            viewModel.onSaveHandled()
            onSaved(savedId, viewModel.isNew)
        }
    }
    val fixFields = stringResource(R.string.project_error_fix_fields)
    val saveFailed = stringResource(UiR.string.ui_error_not_saved)
    val seePro = stringResource(UiR.string.ui_action_see_pro)
    val limitText = pluralStringResource(UiR.plurals.ui_limit_reached, viewModel.limitNotice ?: 0, viewModel.limitNotice ?: 0)
    // Consume the message only after the snackbar is gone: clearing it first would change this
    // effect's key and cancel the snackbar straight away.
    LaunchedEffect(viewModel.message) {
        val message = viewModel.message ?: return@LaunchedEffect
        when (message) {
            EditorMessage.FixFields -> snackbarHostState.showSnackbar(fixFields)
            EditorMessage.SaveFailed -> snackbarHostState.showSnackbar(saveFailed)
            EditorMessage.LimitReached -> {
                val result = snackbarHostState.showSnackbar(limitText, seePro, withDismissAction = true, duration = SnackbarDuration.Long)
                if (result == SnackbarResult.ActionPerformed) onOpenPro()
            }
        }
        viewModel.onMessageShown()
    }

    ProjectEditorScreen(
        isNew = viewModel.isNew,
        loadState = viewModel.loadState,
        form = viewModel.form,
        errors = viewModel.errors,
        isSaving = viewModel.isSaving,
        limitNotice = viewModel.limitNotice,
        snackbarHostState = snackbarHostState,
        callbacks = EditorCallbacks(
            onClose = requestClose,
            onOpenPro = onOpenPro,
            onSave = viewModel::save,
            onName = viewModel::onNameChange,
            onReference = viewModel::onReferenceChange,
            onCustomer = viewModel::onCustomerNameChange,
            onSite = viewModel::onSiteNameChange,
            onAddress = viewModel::onSiteAddressChange,
            onDescription = viewModel::onDescriptionChange,
            onStatus = viewModel::onStatusChange,
            onStartDate = viewModel::onStartDateChange,
            onEndDate = viewModel::onEndDateChange,
        ),
    )

    if (confirmDiscard) {
        PpConfirmDialog(
            title = stringResource(R.string.project_discard_title),
            message = stringResource(R.string.project_discard_message),
            confirmLabel = stringResource(R.string.project_discard_confirm),
            dismissLabel = stringResource(R.string.project_discard_keep),
            onConfirm = {
                confirmDiscard = false
                onClose()
            },
            onDismiss = { confirmDiscard = false },
            destructive = true,
        )
    }
}

internal data class EditorCallbacks(
    val onClose: () -> Unit,
    val onOpenPro: () -> Unit,
    val onSave: () -> Unit,
    val onName: (String) -> Unit,
    val onReference: (String) -> Unit,
    val onCustomer: (String) -> Unit,
    val onSite: (String) -> Unit,
    val onAddress: (String) -> Unit,
    val onDescription: (String) -> Unit,
    val onStatus: (ProjectStatus) -> Unit,
    val onStartDate: (java.time.LocalDate?) -> Unit,
    val onEndDate: (java.time.LocalDate?) -> Unit,
)

@Composable
private fun FieldError?.message(): String? = when (this) {
    null -> null
    FieldError.Required -> stringResource(R.string.project_error_required)
    is FieldError.TooLong -> pluralStringResource(R.plurals.project_error_too_long, maxLength, maxLength)
    FieldError.EndBeforeStart -> stringResource(R.string.project_error_end_before_start)
}

private val wordsNext = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
private val sentences = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)

@Composable
internal fun ProjectEditorScreen(
    isNew: Boolean,
    loadState: EditorLoadState,
    form: ProjectForm,
    errors: Map<ProjectField, FieldError>,
    isSaving: Boolean,
    limitNotice: Int?,
    snackbarHostState: SnackbarHostState,
    callbacks: EditorCallbacks,
) {
    val spacing = PortProofTheme.spacing
    Box {
        PpGradientScreen(
            title = stringResource(if (isNew) R.string.project_editor_new_title else R.string.project_editor_edit_title),
            onBack = callbacks.onClose,
            bottomBar = if (loadState == EditorLoadState.Ready) {
                {
                    PpPrimaryButton(
                        text = stringResource(if (isNew) R.string.project_editor_create else R.string.project_editor_save),
                        onClick = callbacks.onSave,
                        loading = isSaving,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                null
            },
        ) {
            when (loadState) {
                EditorLoadState.Loading -> Unit
                EditorLoadState.NotFound -> PpEmptyState(
                    icon = PortProofIcons.Alert,
                    title = stringResource(R.string.project_not_found_title),
                    message = stringResource(R.string.project_not_found_message),
                )
                EditorLoadState.Ready -> Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = spacing.screenGutter, vertical = spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    if (limitNotice != null) LimitNotice(limitNotice, callbacks.onOpenPro)
                    EditorFields(form, errors, callbacks)
                }
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp))
    }
}

/** Shown while the free plan has no room for the active project this form would add. */
@Composable
private fun LimitNotice(limit: Int, onOpenPro: () -> Unit) {
    val colors = PortProofTheme.colors
    PpCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PpIconTile(PortProofIcons.Crown, size = 40.dp, iconSize = 18.dp, containerColor = colors.accentContainer, tint = colors.onAccentContainer)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    pluralStringResource(UiR.plurals.ui_limit_reached, limit, limit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                )
                PpSecondaryButton(stringResource(R.string.project_editor_see_pro), onOpenPro)
            }
        }
    }
}

@Composable
private fun EditorFields(form: ProjectForm, errors: Map<ProjectField, FieldError>, callbacks: EditorCallbacks) {
    PpCard {
        PpSectionHeader(stringResource(R.string.project_editor_section_project), style = MaterialTheme.typography.titleSmall)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PpTextField(
                value = form.name,
                onValueChange = callbacks.onName,
                label = stringResource(R.string.project_field_name),
                placeholder = stringResource(R.string.project_field_name_placeholder),
                error = errors[ProjectField.NAME].message(),
                keyboardOptions = wordsNext,
            )
            PpTextField(
                value = form.reference,
                onValueChange = callbacks.onReference,
                label = stringResource(R.string.project_field_reference),
                placeholder = stringResource(R.string.project_field_reference_placeholder),
                error = errors[ProjectField.REFERENCE].message(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
            )
            PpSegmentedControl(
                options = ProjectStatus.entries,
                selected = form.status,
                onSelect = callbacks.onStatus,
                label = { stringResource(it.labelRes) },
            )
        }
    }
    PpCard {
        PpSectionHeader(stringResource(R.string.project_section_customer_site), style = MaterialTheme.typography.titleSmall)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PpTextField(form.customerName, callbacks.onCustomer, stringResource(R.string.project_field_customer),
                error = errors[ProjectField.CUSTOMER_NAME].message(), keyboardOptions = wordsNext)
            PpTextField(form.siteName, callbacks.onSite, stringResource(R.string.project_field_site),
                error = errors[ProjectField.SITE_NAME].message(), keyboardOptions = wordsNext)
            PpTextField(form.siteAddress, callbacks.onAddress, stringResource(R.string.project_field_address),
                error = errors[ProjectField.SITE_ADDRESS].message(), singleLine = false, minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words))
        }
    }
    ScheduleFields(form, errors[ProjectField.END_DATE].message(), callbacks)
    PpCard {
        PpSectionHeader(stringResource(R.string.project_section_description), style = MaterialTheme.typography.titleSmall)
        PpTextField(form.description, callbacks.onDescription, stringResource(R.string.project_field_description),
            placeholder = stringResource(R.string.project_field_description_placeholder),
            error = errors[ProjectField.DESCRIPTION].message(), singleLine = false, minLines = 4, maxLines = 12,
            keyboardOptions = sentences)
    }
}
