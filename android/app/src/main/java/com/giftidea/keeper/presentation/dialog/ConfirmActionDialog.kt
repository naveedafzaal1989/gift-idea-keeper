package com.giftidea.keeper.presentation.dialog

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R

@Composable
fun ConfirmActionDialog(title: Int, message: String, confirm: Int, onConfirm: () -> Unit, onDismiss: () -> Unit, saving: Boolean = false, cancel: Int = R.string.cancel) {
    AlertDialog(onDismissRequest = { if (!saving) onDismiss() }, title = { Text(stringResource(title)) }, text = { Text(message) },
        confirmButton = { TextButton(onConfirm, Modifier.heightIn(min = 48.dp), enabled = !saving) { Text(stringResource(confirm)) } },
        dismissButton = { TextButton(onDismiss, Modifier.heightIn(min = 48.dp), enabled = !saving) { Text(stringResource(cancel)) } })
}
