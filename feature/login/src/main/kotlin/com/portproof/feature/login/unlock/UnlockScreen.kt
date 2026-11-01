package com.portproof.feature.login.unlock

import android.app.ActivityManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpConfirmDialog
import com.portproof.core.designsystem.component.PpKeypadIconKey
import com.portproof.core.designsystem.component.PpTextLink
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.feature.login.LoginScaffold
import com.portproof.feature.login.PinPad
import com.portproof.feature.login.R
import com.portproof.feature.login.rememberBiometricPrompt
import kotlinx.coroutines.delay
import java.time.Instant

/** The login screen shown over the app whenever it is locked. */
@Composable
fun UnlockRoute(viewModel: UnlockViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    // Back leaves the app instead of reaching the screens behind the lock.
    BackHandler { activity?.moveTaskToBack(true) }

    val prompt = if (state.biometricEnabled) rememberBiometricPrompt() else null
    var promptedOnce by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(prompt) {
        if (prompt != null && !promptedOnce) {
            promptedOnce = true
            prompt(viewModel::onBiometricSuccess)
        }
    }
    UnlockScreen(
        state = state,
        entered = viewModel.pin.digits.length,
        feedback = viewModel.feedback,
        checking = viewModel.isChecking,
        onDigit = viewModel::onDigit,
        onBackspace = viewModel::onBackspace,
        onBiometric = prompt?.let { { it(viewModel::onBiometricSuccess) } },
    )
}

@Composable
internal fun UnlockScreen(
    state: UnlockUiState,
    entered: Int,
    feedback: UnlockFeedback?,
    checking: Boolean,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: (() -> Unit)?,
) {
    var confirmErase by rememberSaveable { mutableStateOf(false) }
    val remaining = rememberSecondsUntil(state.lockedUntil)
    val lockedOut = remaining > 0
    LoginScaffold(
        title = if (state.technicianName.isEmpty()) {
            stringResource(R.string.login_unlock_title)
        } else {
            stringResource(R.string.login_unlock_title_name, state.technicianName.substringBefore(' '))
        },
        body = stringResource(R.string.login_unlock_body),
        onBack = null,
        centered = true,
    ) {
        Spacer(Modifier.height(4.dp))
        PinPad(
            entered = entered,
            message = when {
                lockedOut -> stringResource(R.string.login_locked_out, formatWait(remaining))
                feedback is UnlockFeedback.Wrong -> pluralStringResource(R.plurals.login_wrong_pin, feedback.attemptsLeft, feedback.attemptsLeft)
                feedback == UnlockFeedback.Error -> stringResource(R.string.login_error_generic)
                else -> null
            },
            isError = lockedOut || feedback != null,
            enabled = !checking && !lockedOut,
            onDigit = onDigit,
            onBackspace = onBackspace,
            leftKey = onBiometric?.let { launch ->
                { PpKeypadIconKey(PortProofIcons.Fingerprint, stringResource(R.string.login_use_fingerprint), enabled = !checking, onClick = launch) }
            },
            footer = { PpTextLink(stringResource(R.string.login_forgot), { confirmErase = true }, color = PortProofTheme.colors.accentText) },
        )
    }
    if (confirmErase) {
        val context = LocalContext.current
        PpConfirmDialog(
            title = stringResource(R.string.login_forgot_title),
            message = stringResource(R.string.login_forgot_message),
            confirmLabel = stringResource(R.string.login_forgot_erase),
            dismissLabel = stringResource(R.string.login_cancel),
            onConfirm = {
                confirmErase = false
                // Deletes the database, preferences and PIN, then closes the app; the next start is a first run.
                context.getSystemService(ActivityManager::class.java)?.clearApplicationUserData()
            },
            onDismiss = { confirmErase = false },
            destructive = true,
        )
    }
}

/** Whole seconds left until [until], updated every second; 0 when there is no wait. */
@Composable
private fun rememberSecondsUntil(until: Instant?): Long {
    var seconds by remember(until) { mutableLongStateOf(secondsLeft(until)) }
    LaunchedEffect(until) {
        while (seconds > 0) {
            delay(1_000)
            seconds = secondsLeft(until)
        }
    }
    return seconds
}

private fun secondsLeft(until: Instant?): Long {
    until ?: return 0
    val millis = until.toEpochMilli() - System.currentTimeMillis()
    return if (millis <= 0) 0 else (millis + 999) / 1_000
}

private fun formatWait(seconds: Long): String = "%d:%02d".format(seconds / 60, seconds % 60)
