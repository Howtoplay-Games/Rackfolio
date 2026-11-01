package com.portproof.feature.login

import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Strong (class 3) fingerprint or face unlock enrolled on this device. */
internal fun biometricsAvailable(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

@Composable
internal fun rememberBiometricsAvailable(): Boolean {
    val context = LocalContext.current
    return remember(context) { biometricsAvailable(context) }
}

/**
 * Returns a function that shows the system biometric prompt and calls back on success, or null when
 * biometrics cannot be used here (no hardware, nothing enrolled, or no fragment host).
 */
@Composable
internal fun rememberBiometricPrompt(): ((onSuccess: () -> Unit) -> Unit)? {
    val activity = LocalActivity.current as? FragmentActivity ?: return null
    if (!rememberBiometricsAvailable()) return null
    val title = stringResource(R.string.login_bio_prompt_title)
    val subtitle = stringResource(R.string.login_bio_prompt_subtitle)
    val cancel = stringResource(R.string.login_bio_prompt_cancel)
    return remember(activity, title, subtitle, cancel) {
        { onSuccess ->
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
                },
            )
            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText(cancel)
                    .setAllowedAuthenticators(BIOMETRIC_STRONG)
                    .setConfirmationRequired(false)
                    .build(),
            )
        }
    }
}
