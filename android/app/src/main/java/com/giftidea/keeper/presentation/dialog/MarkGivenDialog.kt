package com.giftidea.keeper.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkGivenDialog(state: MarkGivenUiState, onChange: (MarkGivenUiState) -> Unit, onStep: (Int) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit) {
    if (state.datePicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.parse(state.date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = java.time.Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate() <= LocalDate.now()
            override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now().year
        })
        DatePickerDialog(onDismissRequest = { onChange(state.copy(datePicker = false)) }, confirmButton = { TextButton({ picker.selectedDateMillis?.let { onChange(state.copy(date = java.time.Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString(), datePicker = false)) } }) { Text(stringResource(R.string.confirm_date)) } }, dismissButton = { TextButton({ onChange(state.copy(datePicker = false)) }) { Text(stringResource(R.string.cancel)) } }) { DatePicker(picker) }
    } else AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.mark_given_title)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(state.title, style = MaterialTheme.typography.titleMedium)
            Text(dateLabel(state.date))
            SecondaryButton(R.string.choose_date, { onChange(state.copy(datePicker = true)) }, enabled = !state.saving)
            ToggleRow(R.string.record_actual, state.amountEnabled, { onChange(state.copy(amountEnabled = it)) }, !state.saving)
            if (state.amountEnabled) MoneyStepper(state.amount, { onChange(state.copy(amount = it, error = null)) }, onStep, state.error == R.string.invalid_amount, enabled = !state.saving, canDecrease = state.canDecreaseAmount, canIncrease = state.canIncreaseAmount)
            state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(onSave, Modifier.heightIn(min = 48.dp), enabled = !state.saving) { Text(stringResource(if (state.saving) R.string.saving_action else R.string.record_gift)) } }, dismissButton = { TextButton(onDismiss, Modifier.heightIn(min = 48.dp), enabled = !state.saving) { Text(stringResource(R.string.cancel)) } })
}
