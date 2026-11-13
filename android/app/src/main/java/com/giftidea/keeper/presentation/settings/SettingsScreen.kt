package com.giftidea.keeper.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.presentation.dialog.ConfirmActionDialog

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onMenu: () -> Unit, onReminder: (Boolean) -> Unit, onLead: (Int) -> Unit, onHaptics: (Boolean) -> Unit, onSave: () -> Unit, onNotifications: () -> Unit, onConfirmClear: (Boolean) -> Unit, onClear: () -> Unit) {
    DecoratedBackground(BackgroundVariant.Form) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().widthIn(max = 560.dp).align(Alignment.TopCenter).padding(horizontal = 20.dp)) {
            ScreenHeader(stringResource(R.string.settings_title), onBack = onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NotebookCard {
                    ToggleRow(R.string.birthday_reminders, state.reminders, onReminder, !state.saving)
                    val leadDescription = stringResource(R.string.reminder_lead, state.leadDays)
                    Text(leadDescription, Modifier.semantics { stateDescription = leadDescription; liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SecondaryButton(R.string.decrease, { onLead(-1) }, Modifier.weight(1f), state.leadDays > 0 && !state.saving); SecondaryButton(R.string.increase, { onLead(1) }, Modifier.weight(1f), state.leadDays < 30 && !state.saving) }
                    Text(if (state.leadDays == 0) stringResource(R.string.reminder_on_birthday) else stringResource(R.string.reminder_time, state.leadDays), style = MaterialTheme.typography.bodyMedium)
                    if (!state.available) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Image(painterResource(R.drawable.icon_pause), null, Modifier.size(24.dp), contentScale = ContentScale.Fit); Text(stringResource(R.string.notifications_off), style = MaterialTheme.typography.bodyMedium) }
                        Text(stringResource(R.string.reminders_here), style = MaterialTheme.typography.bodyMedium)
                        LabelButton(R.string.open_notification_settings, onNotifications)
                    }
                    ToggleRow(R.string.haptic_feedback, state.haptics, onHaptics, !state.saving)
                }
                NotebookCard { Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.privacy_copy), style = MaterialTheme.typography.bodyMedium); Text(stringResource(R.string.app_version, state.version), style = MaterialTheme.typography.labelSmall) }
                if (state.saved) SavedFeedback(R.string.settings_saved)
                if (state.error) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
                Text(stringResource(R.string.only_local), style = MaterialTheme.typography.bodyMedium)
                PrimaryButton(if (state.error) R.string.retry else R.string.save_settings, onSave, loading = state.saving)
                SecondaryButton(R.string.clear_notebook, { onConfirmClear(true) }, Modifier.fillMaxWidth(), !state.saving)
                SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), !state.saving)
            }
        }
    }
    if (state.confirmClear) ConfirmActionDialog(R.string.clear_notebook, stringResource(R.string.clear_notebook_message), R.string.clear_all, onClear, { onConfirmClear(false) }, state.saving)
}
