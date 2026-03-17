package no.nordicsemi.nrf.matter.binding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.ui.DEVICE_LIST_TEST

@Composable
internal fun TargetLightSettings(
    selectedItems: List<Device> = emptyList(),
    onItemSelected: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Box {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { isExpanded = !isExpanded }
        )
        // Show AlertDialog when isExpanded is true
        AnimatedVisibility(isExpanded) {
            // Your AlertDialog content here
            TargetLightSettingsDialog(
                selectedItems = selectedItems,
                onConfirmation = onItemSelected,
                onDismiss = { isExpanded = false }
            )
        }
    }
}

@Composable
internal fun TargetLightSettingsDialog(
    selectedItems: List<Device>,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
) {
    val updateOptions = DEVICE_LIST_TEST // TODO: get all light on/off devices.
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(selectedItems) }

    AlertDialog(
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Select Target lights",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Commissioned Devices",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                Column(Modifier.selectableGroup()) {
                    updateOptions.forEach { text ->
                        OutlinedCard(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (text == selectedOption),
                                        onClick = { },
                                        role = Role.RadioButton
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = text.name ?: "Unknown",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = "${text.deviceType}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                // TODO: ADD the Multiple choice options.

                            }
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.scrim
                    )
                    Text(text = "Warning message")
                }
            }


        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // looks "muted"
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun TargetLightSettingsDialogPreview() {
    TargetLightSettingsDialog(
        selectedItems = DEVICE_LIST_TEST,
        onDismiss = {},
        onConfirmation = {}
    )
}
