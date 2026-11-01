package com.portproof.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpDivider
import com.portproof.core.designsystem.component.PpListRow
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.component.PpSegmentedControl
import com.portproof.core.designsystem.component.PpTextField
import com.portproof.core.designsystem.component.PpListScreen
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.component.PpIconTile
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.model.AppPlan
import com.portproof.core.model.activeProjectLimit
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ThemeMode
import com.portproof.core.model.UserPreferences
import com.portproof.core.ui.R as UiR

@Composable
internal fun SettingsRoute(onOpenPro: () -> Unit, onOpenSecurity: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ready = state as? SettingsUiState.Ready ?: return
    SettingsScreen(
        state = ready,
        appVersion = rememberAppVersion(),
        onUpdateProfile = viewModel::updateProfile,
        onThemeSelected = viewModel::setThemeMode,
        onOpenPro = onOpenPro,
        onOpenSecurity = onOpenSecurity,
    )
}

@Composable
private fun rememberAppVersion(): String {
    val context = LocalContext.current
    return androidx.compose.runtime.remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }
}

@Composable
internal fun SettingsScreen(
    state: SettingsUiState.Ready,
    appVersion: String,
    onUpdateProfile: (ProfileField, String) -> Boolean,
    onThemeSelected: (ThemeMode) -> Unit,
    onOpenPro: () -> Unit,
    onOpenSecurity: () -> Unit,
) {
    val preferences = state.preferences
    val spacing = PortProofTheme.spacing
    var editing by rememberSaveable { mutableStateOf<ProfileField?>(null) }
    PpListScreen {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
        ) {
            PpTopBar(stringResource(R.string.settings_title))
            PlanCard(state.plan, state.activeProjects, onOpenPro)
            ProfileCard(preferences) { editing = it }
            SecurityCard(onOpenSecurity)
            AppearanceCard(preferences.themeMode, onThemeSelected)
            PrivacyCard()
            AboutCard(appVersion)
            Spacer(Modifier.height(spacing.lg))
        }
    }
    editing?.let { field ->
        val current = if (field == ProfileField.TECHNICIAN_NAME) preferences.technicianName else preferences.companyName
        ProfileDialog(field, current, onDismiss = { editing = null }) { value ->
            onUpdateProfile(field, value).also { saved -> if (saved) editing = null }
        }
    }
}

@Composable
private fun ProfileCard(preferences: UserPreferences, onEdit: (ProfileField) -> Unit) {
    val notSet = stringResource(R.string.settings_not_set)
    PpCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        PpSectionHeader(stringResource(R.string.settings_profile), style = MaterialTheme.typography.titleSmall)
        PpListRow(
            title = stringResource(R.string.settings_technician_name),
            icon = PortProofIcons.User,
            value = preferences.technicianName.ifBlank { notSet },
            onClick = { onEdit(ProfileField.TECHNICIAN_NAME) },
        )
        PpDivider()
        PpListRow(
            title = stringResource(R.string.settings_company_name),
            icon = PortProofIcons.Building,
            value = preferences.companyName.ifBlank { notSet },
            onClick = { onEdit(ProfileField.COMPANY_NAME) },
        )
        Text(
            stringResource(R.string.settings_profile_footer),
            style = MaterialTheme.typography.bodySmall,
            color = PortProofTheme.colors.textMuted,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

/** Current plan, with the way to PortProof Pro (or to managing it). */
@Composable
private fun PlanCard(plan: AppPlan, activeProjects: Int, onOpenPro: () -> Unit) {
    val colors = PortProofTheme.colors
    val limit = plan.activeProjectLimit
    PpCard(glow = true) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PpIconTile(PortProofIcons.Crown, size = 44.dp, iconSize = 20.dp, containerColor = colors.accent, tint = colors.onAccent)
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(if (limit == null) R.string.settings_plan_pro else R.string.settings_plan_free),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                )
                Text(
                    if (limit == null) {
                        stringResource(R.string.settings_plan_pro_body)
                    } else {
                        pluralStringResource(R.plurals.settings_plan_free_body, limit, activeProjects.coerceAtMost(limit), limit)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        PpPrimaryButton(
            stringResource(if (limit == null) R.string.settings_plan_manage else R.string.settings_plan_upgrade),
            onOpenPro,
            Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SecurityCard(onOpenSecurity: () -> Unit) {
    PpCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        PpListRow(
            title = stringResource(R.string.settings_security),
            icon = PortProofIcons.Lock,
            value = stringResource(R.string.settings_security_body),
            onClick = onOpenSecurity,
        )
    }
}

@Composable
private fun AppearanceCard(mode: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    PpCard {
        PpSectionHeader(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.titleSmall)
        PpSegmentedControl(
            options = ThemeMode.entries,
            selected = mode,
            onSelect = onThemeSelected,
            label = {
                stringResource(
                    when (it) {
                        ThemeMode.SYSTEM -> R.string.settings_theme_system
                        ThemeMode.LIGHT -> R.string.settings_theme_light
                        ThemeMode.DARK -> R.string.settings_theme_dark
                    },
                )
            },
        )
    }
}

@Composable
private fun PrivacyCard() {
    PpCard {
        PpSectionHeader(stringResource(R.string.settings_privacy), style = MaterialTheme.typography.titleSmall)
        InfoBlock(PortProofIcons.ShieldCheck, stringResource(R.string.settings_privacy_local_title), stringResource(R.string.settings_privacy_local_body))
        Spacer(Modifier.height(12.dp))
        InfoBlock(PortProofIcons.Smartphone, stringResource(R.string.settings_privacy_backup_title), stringResource(R.string.settings_privacy_backup_body))
    }
}

@Composable
private fun InfoBlock(icon: ImageVector, title: String, body: String) {
    val colors = PortProofTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(body, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
}

@Composable
private fun AboutCard(appVersion: String) {
    PpCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        PpSectionHeader(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleSmall)
        PpListRow(title = stringResource(R.string.settings_version), icon = PortProofIcons.Info, value = appVersion)
    }
}

@Composable
private fun ProfileDialog(field: ProfileField, current: String, onDismiss: () -> Unit, onSave: (String) -> Boolean) {
    var text by rememberSaveable(field) { mutableStateOf(current) }
    var tooLong by rememberSaveable(field) { mutableStateOf(false) }
    val colors = PortProofTheme.colors
    val label = stringResource(if (field == ProfileField.TECHNICIAN_NAME) R.string.settings_technician_name else R.string.settings_company_name)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(label, style = MaterialTheme.typography.titleLarge) },
        text = {
            PpTextField(
                value = text,
                onValueChange = { text = it; tooLong = false },
                label = label,
                error = if (tooLong) pluralStringResource(R.plurals.settings_name_too_long, field.maxLength, field.maxLength) else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        },
        confirmButton = {
            TextButton(onClick = { tooLong = !onSave(text) }) {
                Text(stringResource(UiR.string.ui_action_save), color = colors.accentText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(UiR.string.ui_action_cancel), color = colors.textSecondary) }
        },
    )
}
