package com.portproof.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.component.PpCanvas
import com.portproof.core.designsystem.component.PpKeypad
import com.portproof.core.designsystem.component.PpPinDots
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.PinRules

/** Canvas, optional back button, large title and text, then [content]; shared by the login screens. */
@Composable
internal fun LoginScaffold(
    title: String,
    body: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PortProofTheme.colors
    PpCanvas(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            PpTopBar(title = null, onBack = onBack)
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            )
            if (body != null) {
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            content()
        }
    }
}

/** PIN dots, a message line under them, and the keypad pinned to the bottom. */
@Composable
internal fun ColumnScope.PinPad(
    entered: Int,
    message: String?,
    isError: Boolean,
    enabled: Boolean,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    leftKey: (@Composable RowScope.() -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    Spacer(Modifier.weight(1f))
    PpPinDots(
        length = PinRules.LENGTH,
        filled = entered,
        description = pluralStringResource(R.plurals.login_digits_entered, entered, entered, PinRules.LENGTH),
        error = isError,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Text(
        message.orEmpty(),
        style = MaterialTheme.typography.bodySmall,
        color = if (isError) colors.dangerText else colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .widthIn(max = 360.dp)
            .heightIn(min = 40.dp)
            .padding(top = 14.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
    Spacer(Modifier.weight(1f))
    PpKeypad(
        onDigit = onDigit,
        onBackspace = onBackspace,
        backspaceLabel = stringResource(R.string.login_backspace),
        enabled = enabled,
        leftKey = leftKey,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Column(Modifier.fillMaxWidth().heightIn(min = 56.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        footer?.invoke()
    }
}
