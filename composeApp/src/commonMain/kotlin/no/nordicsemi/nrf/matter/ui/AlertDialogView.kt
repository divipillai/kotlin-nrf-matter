package no.nordicsemi.nrf.matter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AlertDialogView(
    title: String,
    message: String,
    dismissText: String = "Cancel",
    confirmText: String = "Delete",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = title)
            }
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(dismissText)
            }
        }
    )
}
