package com.portproof.feature.login.setup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.portproof.core.designsystem.component.PpIconTile
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.designsystem.component.PpSecondaryButton
import com.portproof.core.designsystem.component.PpTextField
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProfileRules
import com.portproof.feature.login.LoginScaffold
import com.portproof.feature.login.PinPad
import com.portproof.feature.login.R
import com.portproof.feature.login.rememberBiometricPrompt
import com.portproof.feature.login.welcome.WelcomeScreen

/** First-run flow shown instead of the app until a PIN exists. */
@Composable
fun LoginSetupRoute(viewModel: SetupViewModel = hiltViewModel()) {
    val step = viewModel.step
    BackHandler(enabled = step != SetupStep.Welcome) { viewModel.onBack() }
    val prompt = rememberBiometricPrompt()
    LaunchedEffect(prompt) { viewModel.offerBiometrics = prompt != null }
    when (step) {
        SetupStep.Welcome -> WelcomeScreen(onGetStarted = viewModel::onGetStarted)
        SetupStep.Profile -> ProfileStep(viewModel)
        SetupStep.CreatePin, SetupStep.ConfirmPin -> PinStep(viewModel)
        SetupStep.Biometric -> BiometricStep(
            saving = viewModel.isSaving,
            error = viewModel.error,
            onTurnOn = { if (prompt != null) prompt { viewModel.finish(useBiometrics = true) } else viewModel.finish(useBiometrics = false) },
            onNotNow = { viewModel.finish(useBiometrics = false) },
            onBack = { viewModel.onBack() },
        )
    }
}

@Composable
private fun ProfileStep(viewModel: SetupViewModel) {
    val error = viewModel.error
    LoginScaffold(
        title = stringResource(R.string.login_setup_profile_title),
        body = stringResource(R.string.login_setup_profile_body),
        onBack = { viewModel.onBack() },
    ) {
        Column(Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PpTextField(
                value = viewModel.name,
                onValueChange = viewModel::onNameChange,
                label = stringResource(R.string.login_field_name),
                error = when (error) {
                    SetupError.NameRequired -> stringResource(R.string.login_error_name_required)
                    SetupError.NameTooLong -> pluralStringResource(
                        R.plurals.login_error_too_long, ProfileRules.TECHNICIAN_NAME_MAX_LENGTH, ProfileRules.TECHNICIAN_NAME_MAX_LENGTH,
                    )
                    else -> null
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
            PpTextField(
                value = viewModel.company,
                onValueChange = viewModel::onCompanyChange,
                label = stringResource(R.string.login_field_company),
                error = if (error == SetupError.CompanyTooLong) {
                    pluralStringResource(R.plurals.login_error_too_long, ProfileRules.COMPANY_NAME_MAX_LENGTH, ProfileRules.COMPANY_NAME_MAX_LENGTH)
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            )
        }
        Spacer(Modifier.weight(1f))
        PpPrimaryButton(
            stringResource(R.string.login_continue),
            viewModel::onProfileContinue,
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
        )
    }
}

@Composable
private fun PinStep(viewModel: SetupViewModel) {
    val confirming = viewModel.step == SetupStep.ConfirmPin
    val error = viewModel.error
    LoginScaffold(
        title = stringResource(if (confirming) R.string.login_setup_confirm_title else R.string.login_setup_pin_title),
        body = stringResource(if (confirming) R.string.login_setup_confirm_body else R.string.login_setup_pin_body),
        onBack = { viewModel.onBack() },
    ) {
        PinPad(
            entered = viewModel.pin.digits.length,
            message = when (error) {
                SetupError.PinTooSimple -> stringResource(R.string.login_error_pin_simple)
                SetupError.PinMismatch -> stringResource(R.string.login_error_pin_mismatch)
                SetupError.SaveFailed -> stringResource(R.string.login_error_save)
                else -> null
            },
            isError = error != null,
            enabled = !viewModel.isSaving,
            onDigit = viewModel::onDigit,
            onBackspace = viewModel::onBackspace,
        )
    }
}

@Composable
private fun BiometricStep(saving: Boolean, error: SetupError?, onTurnOn: () -> Unit, onNotNow: () -> Unit, onBack: () -> Unit) {
    val colors = PortProofTheme.colors
    LoginScaffold(
        title = stringResource(R.string.login_setup_bio_title),
        body = stringResource(R.string.login_setup_bio_body),
        onBack = onBack,
    ) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            PpIconTile(PortProofIcons.Fingerprint, size = 120.dp, iconSize = 56.dp, containerColor = colors.accentContainer, tint = colors.onAccentContainer)
        }
        if (error == SetupError.SaveFailed) {
            Text(stringResource(R.string.login_error_save), style = MaterialTheme.typography.bodySmall, color = colors.dangerText)
            Spacer(Modifier.height(8.dp))
        }
        PpPrimaryButton(stringResource(R.string.login_bio_turn_on), onTurnOn, Modifier.fillMaxWidth(), loading = saving)
        Spacer(Modifier.height(10.dp))
        PpSecondaryButton(stringResource(R.string.login_bio_not_now), onNotNow, Modifier.fillMaxWidth().padding(bottom = 16.dp), enabled = !saving)
        Spacer(Modifier.size(0.dp))
    }
}
