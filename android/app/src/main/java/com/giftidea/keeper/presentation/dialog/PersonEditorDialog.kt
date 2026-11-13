package com.giftidea.keeper.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*

@Composable
fun PersonEditorDialog(state: PersonEditorUiState, onChange: (PersonEditorAction) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null) {
    val months = listOf(R.string.january, R.string.february, R.string.march, R.string.april, R.string.may, R.string.june, R.string.july, R.string.august, R.string.september, R.string.october, R.string.november, R.string.december)
    var monthMenu by remember { mutableStateOf(false) }
    if (state.confirmDelete && onDelete != null) {
        ConfirmActionDialog(R.string.delete_person, stringResource(R.string.delete_person_message, state.name), R.string.delete_person, onDelete, { onChange(PersonEditorAction.CancelDelete) }, state.saving)
    } else AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(if (state.id == null) R.string.add_person_title else R.string.edit_person)) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(state.name, { onChange(PersonEditorAction.Name(it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.person_name)) }, singleLine = true, enabled = !state.saving, isError = state.error == R.string.invalid_person)
                OutlinedTextField(state.relationship, { onChange(PersonEditorAction.Relationship(it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.relationship)) }, singleLine = true, enabled = !state.saving)
                Text(stringResource(R.string.birthday), style = MaterialTheme.typography.titleMedium)
                Box {
                    SecondaryButton(months[state.month - 1], { monthMenu = true }, Modifier.fillMaxWidth(), !state.saving)
                    DropdownMenu(monthMenu, { monthMenu = false }) { months.forEachIndexed { index, label -> DropdownMenuItem(text = { Text(stringResource(label)) }, onClick = { onChange(PersonEditorAction.BirthdayMonth(index + 1)); monthMenu = false }) } }
                }
                Text(stringResource(R.string.day_of_month, state.day), Modifier.semantics { liveRegion = LiveRegionMode.Polite; stateDescription = state.day.toString() }, style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SecondaryButton(R.string.decrease, { onChange(PersonEditorAction.DayStep(-1)) }, Modifier.weight(1f), state.canDecreaseDay && !state.saving); SecondaryButton(R.string.increase, { onChange(PersonEditorAction.DayStep(1)) }, Modifier.weight(1f), state.canIncreaseDay && !state.saving) }
                Text(stringResource(R.string.no_birth_year), style = MaterialTheme.typography.bodyMedium)
                state.error?.let { Text(stringResource(it), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error) }
                if (onDelete != null) LabelButton(R.string.delete_person, onDelete, enabled = !state.saving)
            }
        }, confirmButton = { TextButton(onSave, Modifier.heightIn(min = 48.dp), enabled = !state.saving) { Text(stringResource(if (state.saving) R.string.saving_action else R.string.save_person)) } },
        dismissButton = { TextButton(onDismiss, Modifier.heightIn(min = 48.dp), enabled = !state.saving) { Text(stringResource(R.string.cancel)) } })
}
