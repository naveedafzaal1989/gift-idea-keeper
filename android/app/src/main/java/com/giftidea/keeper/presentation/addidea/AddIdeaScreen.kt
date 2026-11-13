package com.giftidea.keeper.presentation.addidea

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.presentation.dialog.ConfirmActionDialog

@Composable
fun AddIdeaScreen(state: AddIdeaUiState, onEdit: (AddIdeaUiState) -> Unit, onStep: (Int) -> Unit, onSave: () -> Unit, onBack: () -> Unit, onRetry: () -> Unit, onDiscard: () -> Unit, onKeep: () -> Unit, onDelete: (Boolean) -> Unit) {
    val titleFocus = remember { FocusRequester() }
    val noteFocus = remember { FocusRequester() }
    val linkFocus = remember { FocusRequester() }
    val amountFocus = remember { FocusRequester() }
    LaunchedEffect(state.invalidField) { when (state.invalidField) { 1 -> titleFocus.requestFocus(); 2 -> noteFocus.requestFocus(); 3 -> amountFocus.requestFocus(); 4 -> linkFocus.requestFocus() } }
    DecoratedBackground(BackgroundVariant.Form) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().widthIn(max = 560.dp).align(Alignment.TopCenter).padding(horizontal = 20.dp)) {
            ScreenHeader(stringResource(if (state.editing) R.string.edit_idea_title else R.string.add_idea_title), stringResource(R.string.for_person, state.personName), onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when {
                    state.loading -> Text(stringResource(R.string.loading_notebook))
                    state.missing -> EmptyStateCard(R.string.idea_missing, R.string.back, onBack)
                    state.error == R.string.read_error -> EmptyStateCard(R.string.read_error, R.string.retry, onRetry)
                    else -> {
                        NotebookCard {
                            OutlinedTextField(state.title, { onEdit(state.copy(title = it)) }, Modifier.fillMaxWidth().focusRequester(titleFocus), label = { Text(stringResource(R.string.idea_title)) }, enabled = !state.saving, isError = state.invalidField == 1, supportingText = { Text(stringResource(R.string.character_count, state.title.length, 80)) })
                            OutlinedTextField(state.note, { onEdit(state.copy(note = it)) }, Modifier.fillMaxWidth().heightIn(min = 88.dp).focusRequester(noteFocus), label = { Text(stringResource(R.string.price_note)) }, enabled = !state.saving, isError = state.invalidField == 2, supportingText = { Text(stringResource(R.string.character_count, state.note.length, 160)) }, minLines = 2)
                            ToggleRow(R.string.add_estimate, state.estimateEnabled, { onEdit(state.copy(estimateEnabled = it)) }, !state.saving)
                            if (state.estimateEnabled) MoneyStepper(state.amount, { onEdit(state.copy(amount = it)) }, onStep, state.invalidField == 3, Modifier.focusRequester(amountFocus), !state.saving, state.canDecreaseAmount, state.canIncreaseAmount)
                            OutlinedTextField(state.link, { onEdit(state.copy(link = it)) }, Modifier.fillMaxWidth().focusRequester(linkFocus), label = { Text(stringResource(R.string.reference_link)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri), enabled = !state.saving, isError = state.invalidField == 4, supportingText = { Text(stringResource(R.string.link_hint)) })
                            state.error?.let { Text(stringResource(it), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error) }
                        }
                        Text(stringResource(R.string.only_local), style = MaterialTheme.typography.bodyMedium)
                        PrimaryButton(R.string.save_idea, onSave, loading = state.saving)
                        if (state.editing) SecondaryButton(R.string.delete_idea, { onDelete(false) }, Modifier.fillMaxWidth(), !state.saving)
                    }
                }
            }
        }
    }
    if (state.confirmDiscard) ConfirmActionDialog(R.string.discard_title, stringResource(R.string.discard_message), R.string.discard, onDiscard, onKeep, cancel = R.string.keep_editing)
    if (state.confirmDelete) ConfirmActionDialog(R.string.delete_idea, stringResource(R.string.delete_idea_message), R.string.delete_idea, { onDelete(true) }, onKeep, state.saving)
}
