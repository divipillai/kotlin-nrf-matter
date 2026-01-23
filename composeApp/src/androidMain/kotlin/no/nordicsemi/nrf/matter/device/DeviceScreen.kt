package no.nordicsemi.nrf.matter.device

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.cloudy.cloudy
import no.nordicsemi.nrf.matter.R
import no.nordicsemi.nrf.matter.home.MatterGreen
import no.nordicsemi.nrf.matter.ui.AlertDialogView
import no.nordicsemi.nrf.matter.ui.DeviceItemContainer
import no.nordicsemi.nrf.matter.ui.Loader
import no.nordicsemi.nrf.matter.ui.SectionTitle
import org.koin.androidx.compose.koinViewModel

/*
 * Copyright (c) 2025, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * The Device Screen shows all the information about the device that was selected in the Home
 * screen. It supports the following actions:
 * ```
 * - toggle the on/off state of the device
 * - share the device with another Matter commissioner app
 * - remove the device
 * - inspect the device (get all info we can from the clusters supported by the device)
 * ```
 *
 * When the screen is shown, state monitoring is activated to get the device's latest state. This
 * makes it possible to update the device's online status dynamically.
 */
@Composable
internal fun DeviceScreen(
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    navigateToHome: () -> Unit,
    navigateToInspect: (deviceId: Long) -> Unit,
    updateTitle: (title: String) -> Unit,
    deviceId: Long,
) {
    val deviceViewModel: DeviceViewModel = koinViewModel()
    val deviceUiState by deviceViewModel.deviceUiState.collectAsStateWithLifecycle()
    val deviceUiModel = deviceUiState.deviceUiModel
    var isRemoving by rememberSaveable { mutableStateOf(false) }

    val onOnOffClick: (deviceId: Long, value: Boolean) -> Unit = remember {
        { deviceId, value ->
            deviceViewModel.updateDevicePowerState(deviceId, value)
        }
    }

    LaunchedEffect(Unit) {
        deviceViewModel.loadDevice(deviceId)
        updateTitle("Device")

    }

    if (deviceUiModel == null) {
        Text("Still loading the device information")
        return
    }

    when (deviceUiState.removeDeviceState) {
        RemoveDeviceState.ConfirmRemove -> {
            AlertDialogView(
                onDismiss = { deviceViewModel.updateRemoveDeviceState(RemoveDeviceState.Idle) },
                onConfirm = { deviceViewModel.removeDevice(deviceUiModel.device.deviceId) },
                title = "Remove Device",
                message = "Are you sure you want to remove this device from your Matter network?"
            )
        }

        is RemoveDeviceState.ForceRemove -> {
            // Show a dialog to confirm a force removal.
            // if confirmed, remove device, else do nothing.
            AlertDialogView(
                onDismiss = { deviceViewModel.updateRemoveDeviceState(RemoveDeviceState.Idle) },
                onConfirm = {
                    deviceViewModel.removeDeviceWithoutUnlink(deviceUiModel.device.deviceId)
                },
                title = "Force Remove Device",
                message = "The device could not be removed normally. Do you want to force remove it from your Matter network?"
            )
        }

        RemoveDeviceState.Idle -> {
            // Do nothing.
            isRemoving = false
        }

        is RemoveDeviceState.Removed -> {
            isRemoving = false
            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar("Device removed successfully.")
                navigateToHome()
            }
        }

        RemoveDeviceState.Removing -> {
            isRemoving = true
            Loader {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Removing device...",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "It might take a few seconds, please wait!",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .then(if (isRemoving) Modifier.cloudy() else Modifier)
    ) {

        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {

            DeviceHeader()

            PowerCard(
                enabled = deviceUiModel.isOn,
                onToggle = {
                    onOnOffClick(deviceUiModel.device.deviceId, it)
                }
            )

            SectionTitle("Sharing")
            ShareCard { /* todo: Add share device feature. */ }

            SectionTitle("Technical Details")
            TechnicalDetailsCard()

            Spacer(modifier = Modifier.height(16.dp))
            RemoveDeviceSection { deviceViewModel.updateRemoveDeviceState(RemoveDeviceState.ConfirmRemove) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.light_fixture),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Text(
            "Living Room Light",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MatterGreen, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text("Online", color = MatterGreen)
            Spacer(Modifier.width(8.dp))
            Text("•", color = Color.Gray)
            Spacer(Modifier.width(8.dp))
            Text("Matter Device", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PowerCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    DeviceItemContainer(
        icon = painterResource(R.drawable.power_settings),
        title = "Power",
        subtitle = "Turn device ON or OFF",
        isOnline = enabled,
        onDeviceClick = { },
    ) {
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PowerCardPreview() {
    PowerCard(enabled = true) { }
}

@Composable
private fun ShareCard(onShare: () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(enabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onShare() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Share with other apps",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "You can share this device to control it from other apps or services.",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                )
            }

            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareCardPreview() {
    ShareCard { }
}

@Preview(showBackground = true)
@Composable
private fun TechnicalDetailsCard() {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(enabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            DetailRow("Vendor ID", "0x1234")
            DetailRow("Product ID", "0xABCD")
            DetailRow("Device Type", "Dimmable Light")
            DetailRow("Added Date", "Oct 24, 2023", divider = false)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    divider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier
                .alpha(0.5f)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
        )
    }

    if (divider) HorizontalDivider(
        modifier = Modifier.alpha(0.3f)
    )
}

@Preview(showBackground = true)
@Composable
private fun RemoveDeviceSection(onRemove: () -> Unit = {}) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(enabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onRemove() }
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Remove Device", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                "Removing this device will disconnect it from your Matter fabric and home network.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.alpha(0.5f)
            )
        }
    }
}


