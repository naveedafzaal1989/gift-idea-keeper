package com.giftidea.keeper.presentation.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditorSheet(state: BudgetEditorUiState, onDraft: (BudgetEditorUiState) -> Unit, onStep: (Int) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.set_birthday_budget), style = MaterialTheme.typography.headlineMedium)
            ToggleRow(R.string.set_limit, state.enabled, { onDraft(state.copy(enabled = it)) }, !state.saving)
            if (state.enabled) MoneyStepper(state.amount, { onDraft(state.copy(amount = it, error = false)) }, onStep, state.error, enabled = !state.saving, canDecrease = state.canDecreaseAmount, canIncrease = state.canIncreaseAmount)
            if (state.storageError) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
            Text(stringResource(R.string.budget_does_not_select), style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(R.string.save_budget, onSave, loading = state.saving)
        }
    }
}
