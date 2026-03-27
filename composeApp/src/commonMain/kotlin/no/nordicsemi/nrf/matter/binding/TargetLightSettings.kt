package no.nordicsemi.nrf.matter.binding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                onConfirmation = onItemSelected,
                onDismiss = { isExpanded = false }
            )
        }
    }
}

@Composable
internal fun TargetLightSettingsDialog(
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
) {
    val updateOptions = DEVICE_LIST_TEST // TODO: get all light on/off devices.
    val selectedOptions = remember {
        mutableStateListOf(DEVICE_LIST_TEST.first())
    }
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
                    updateOptions.forEach { device ->
                        OutlinedCard(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedOptions.contains(device),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedOptions.add(device)
                                        } else {
                                            selectedOptions.remove(device)
                                        }
                                    }
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = device.name ?: "Unknown",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = "${device.productName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }


                            }
                        }
                    }
                }
            }
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmation()
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
        onDismiss = {},
        onConfirmation = {}
    )
}
