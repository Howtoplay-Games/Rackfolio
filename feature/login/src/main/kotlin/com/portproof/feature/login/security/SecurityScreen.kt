package com.portproof.feature.login.security

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpDivider
import com.portproof.core.designsystem.component.PpListRow
import com.portproof.core.designsystem.component.PpListScreen
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.AppLockSettings
import com.portproof.feature.login.LoginScaffold
import com.portproof.feature.login.PinPad
import com.portproof.feature.login.R
import com.portproof.feature.login.rememberBiometricPrompt
import com.portproof.feature.login.rememberBiometricsAvailable

/** "Login & security", opened from Settings. */
@Composable
fun SecurityRoute(onBack: () -> Unit, viewModel: SecurityViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val texts = mapOf(
        SecurityMessage.WrongPin to stringResource(R.string.login_msg_wrong_current),
        SecurityMessage.LockedOut to stringResource(R.string.login_msg_locked_out),
        SecurityMessage.TooSimple to stringResource(R.string.login_error_pin_simple),
        SecurityMessage.Mismatch to stringResource(R.string.login_error_pin_mismatch),
        SecurityMessage.PinChanged to stringResource(R.string.login_msg_pin_changed),
        SecurityMessage.Error to stringResource(R.string.login_error_save),
    )
    val step = viewModel.changeStep
    // Messages about PIN entry show under the dots; the others as a snackbar.
    val inlineMessage = viewModel.message?.takeIf { step != null && it != SecurityMessage.PinChanged }
    LaunchedEffect(viewModel.message, step) {
        val message = viewModel.message ?: return@LaunchedEffect
        if (step == null || message == SecurityMessage.PinChanged) {
            snackbarHostState.showSnackbar(texts.getValue(message))
            viewModel.onMessageShown()
        }
    }
    BackHandler(enabled = step != null) { viewModel.cancelChangePin() }

    Box(Modifier.fillMaxSize()) {
        if (step != null) {
            LoginScaffold(
                title = stringResource(
                    when (step) {
                        ChangePinStep.Current -> R.string.login_change_current
                        ChangePinStep.New -> R.string.login_change_new
                        ChangePinStep.Confirm -> R.string.login_change_confirm
                    },
                ),
                body = null,
                onBack = viewModel::cancelChangePin,
            ) {
                PinPad(
                    entered = viewModel.pin.digits.length,
                    message = inlineMessage?.let(texts::getValue),
                    isError = inlineMessage != null,
                    enabled = !viewModel.isBusy,
                    onDigit = { viewModel.onMessageShown(); viewModel.onDigit(it) },
                    onBackspace = viewModel::onBackspace,
                )
            }
        } else {
            SecurityScreen(
                settings = settings,
                onBack = onBack,
                onChangePin = viewModel::startChangePin,
                onBiometricChange = viewModel::setBiometricEnabled,
                onLockNow = viewModel::lockNow,
            )
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
    }
}

@Composable
internal fun SecurityScreen(
    settings: AppLockSettings,
    onBack: () -> Unit,
    onChangePin: () -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onLockNow: () -> Unit,
) {
    val colors = PortProofTheme.colors
    val spacing = PortProofTheme.spacing
    val available = rememberBiometricsAvailable()
    val prompt = rememberBiometricPrompt()
    PpListScreen {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
        ) {
            PpTopBar(stringResource(R.string.login_security_title), onBack = onBack)
            PpCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                PpListRow(
                    title = stringResource(R.string.login_change_pin),
                    icon = PortProofIcons.Key,
                    value = stringResource(R.string.login_change_pin_body),
                    onClick = onChangePin,
                )
                PpDivider()
                PpListRow(
                    title = stringResource(R.string.login_fingerprint),
                    icon = PortProofIcons.Fingerprint,
                    value = stringResource(if (available) R.string.login_fingerprint_body else R.string.login_fingerprint_unavailable),
                    trailing = {
                        Switch(
                            checked = settings.biometricEnabled && available,
                            enabled = available,
                            onCheckedChange = { on ->
                                // Turning it on requires one successful scan, so it is known to work.
                                if (on) prompt?.invoke { onBiometricChange(true) } else onBiometricChange(false)
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.accent, checkedThumbColor = colors.onAccent),
                        )
                    },
                )
                PpDivider()
                PpListRow(title = stringResource(R.string.login_lock_now), icon = PortProofIcons.Lock, onClick = onLockNow)
            }
            Text(
                stringResource(R.string.login_lock_info),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(spacing.lg))
        }
    }
}
