package no.nordicsemi.nrf.matter.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.cloudy.cloudy
import io.github.aakira.napier.Napier
import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.showToast
import no.nordicsemi.nrf.matter.binding.BindingLoaderDialog
import no.nordicsemi.nrf.matter.binding.BindingUiStates
import no.nordicsemi.nrf.matter.binding.LightSwitchBindingCard
import no.nordicsemi.nrf.matter.device.DevicePresenter
import no.nordicsemi.nrf.matter.device.RemoveDeviceState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.theme.NordicSun
import no.nordicsemi.nrf.matter.ui.AlertDialogView
import no.nordicsemi.nrf.matter.ui.DeviceControlItem
import no.nordicsemi.nrf.matter.ui.Loader
import no.nordicsemi.nrf.matter.ui.LockItem
import no.nordicsemi.nrf.matter.ui.SectionTitle
import no.nordicsemi.nrf.matter.utils.toDateString
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.light_bulb
import nrfmatterformobile.composeapp.generated.resources.light_fixture
import nrfmatterformobile.composeapp.generated.resources.no_matter_devices
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.getKoin

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

val MatterGreen = Color(0xFF22C55E)

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
fun DeviceScreen(
    deviceId: DeviceId,
    padding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val devicePresenter: DevicePresenter = getKoin().get()
    val uiState by devicePresenter.uiState.collectAsState()
    var isRemoving by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(deviceId) {
        devicePresenter.observeDevice(deviceId)
    }
    if (uiState.deviceUiModel == null) {
        Text("Still loading the device information")
        return
    }

    val device = uiState.deviceUiModel ?: run {
        Text("Loading device…")
        return
    }
    val bindingState by devicePresenter.bindingState.collectAsState()

    when (uiState.removeDeviceState) {

        RemoveDeviceState.ConfirmRemove -> {
            isRemoving = true
            AlertDialogView(
                onDismiss = { devicePresenter.updateRemoveDeviceState(RemoveDeviceState.Idle) },
                onConfirm = { devicePresenter.removeDevice(device.device.deviceId) },
                title = "Remove Device",
                message = "Are you sure you want to remove this device?"
            )
        }

        is RemoveDeviceState.ForceRemove -> {
            isRemoving = true
            AlertDialogView(
                onDismiss = { devicePresenter.updateRemoveDeviceState(RemoveDeviceState.Idle) },
                onConfirm = {
                    devicePresenter.removeDeviceWithoutUnlink(device.device.deviceId)
                },
                title = "Force Remove Device",
                message = "Unable to unlink device. Force remove?"
            )
        }

        is RemoveDeviceState.Removed -> {
            isRemoving = false
            LaunchedEffect(true) {
                snackbarHostState.showSnackbar("Device removed")
            }
            devicePresenter.updateRemoveDeviceState(RemoveDeviceState.Idle)
            onBack()
        }

        RemoveDeviceState.Removing -> {
            isRemoving = true
            Loader {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Removing device...",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "It might take a few seconds, please wait!",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        RemoveDeviceState.Idle -> {
            isRemoving = false
        }
    }
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxWidth()
            .then(if (isRemoving) Modifier.cloudy() else Modifier)
            .then(if (bindingState is BindingUiStates.InProgress) Modifier.cloudy() else Modifier)
    ) {
        DeviceDetails(device, devicePresenter)
    }

}

@Composable
private fun DeviceDetails(
    device: DeviceUiModel,
    devicePresenter: DevicePresenter
) {
    val targetDevices = devicePresenter.getTargetDevices()
    val selectedDevices = remember { mutableListOf<Device>() }
    Napier.i("target Devices: $targetDevices", tag = "AAA")
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
    ) {

        DeviceHeader(device.device.name ?: "Device")

        DeviceControlSection(device, devicePresenter)

        if (device.device.deviceType == DeviceType.LIGHT_SWITCH) {
            val bindingState by devicePresenter.bindingState.collectAsState()
            when (bindingState) {
                is BindingUiStates.Error -> {
                    AlertDialogView(
                        onDismiss = {
                            // Change state to idle.
                            devicePresenter.updateBindingState(BindingUiStates.Idle)
                        },
                        onConfirm = {
                            // Retry binding.
                            devicePresenter.initiateBinding(
                                device.device.deviceId,
                                selectedDevices.toList()
                            )
                        },
                        title = "Binding Failed.",
                        message = "Unable to bind the device, please try again.",
                        confirmText = "Retry"
                    )
                }

                BindingUiStates.Idle -> {
                    LightSwitchBindingCard(
                        boundDevices = device.boundLights,
                        targetDevices = targetDevices
                    ) {
                        // TODO: Call the callback function here
                        Napier.i { "AAA, LightSwitchBindingCard() called" }
                        selectedDevices.addAll(it)
                        devicePresenter.initiateBinding(
                            sourceNodeId = device.device.deviceId,
                            targetDevices = it
                        )
                    }
                }

                BindingUiStates.InProgress -> {
                    BindingLoaderDialog(dummyLogsForBinding) {
                        // Text Content
                        Text(
                            text = "Binding...",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = NordicSun
                            )
                            Text(text = "Binding in progress, it might take few seconds. Please don't close the app")
                        }
                    }
                }

                is BindingUiStates.Success -> {
                    // TODO: Show success message and show bonded lights in the UI.
                    // Show a Toast of success binding.
                    Napier.i { "AAA, Success" }
                    devicePresenter.updateBindingState(BindingUiStates.Idle)
                    showToast(
                        message = "Binding completed successfully!",
                        duration = ToastDuration.Long,
                        gravity = ToastGravity.Center
                    )
                }
            }
        }

        SectionTitle("Sharing")
        ShareCard {}

        SectionTitle("Technical Details")
        TechnicalDetailsCard(device.device)

        Spacer(Modifier.height(16.dp))

        RemoveDeviceSection {
            devicePresenter.updateRemoveDeviceState(RemoveDeviceState.ConfirmRemove)
        }
    }
}

@Composable
private fun DeviceControlSection(
    device: DeviceUiModel,
    presenter: DevicePresenter
) {
    when (device.device.deviceType) {

        DeviceType.LIGHT_ON_OFF,
        DeviceType.DIMMABLE_LIGHT,
        DeviceType.COLOR_TEMPERATURE_LIGHT,
        DeviceType.EXTENDED_COLOR_LIGHT -> {

            DeviceControlItem(
                deviceId = device.device.deviceId,
                title = "Light",
                subtitle = "Turn light ON or OFF",
                icon = painterResource(Res.drawable.light_bulb),
                enabled = device.isOn,
                updateDeviceState = { id, value ->
                    presenter.togglePower(id, value)
                },
                onClick = {}
            )
        }

        DeviceType.LIGHT_SWITCH,
        DeviceType.OUTLET -> {
            // Do nothing. Since we bind the device to the switch, and it will not send any control commands.
        }

        DeviceType.DOOR_LOCK -> {
            LockItem(
                deviceId = device.device.deviceId,
                title = "Front Door",
                subtitle = "Smart Lock",
                isLocked = device.isOn,
                onLockUnlockDoor = { id, value ->
                    presenter.togglePower(id, value)
                },
                onDeviceClick = {}
            )
        }

        else -> Unit
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceHeader(
    header: String = "Living Room Light"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.light_fixture),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Text(
            header,
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
fun ShareCard(onShare: () -> Unit) {
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

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun ShareCardPreview() {
    ShareCard { }
}

@Composable
private fun TechnicalDetailsCard(device: Device) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(enabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            DetailRow("Vendor ID", device.vendorId ?: "N/A")
            DetailRow("Product ID", device.productId ?: "N/A")
            DetailRow("Product Name", device.productName ?: "N/A")
            device.vendorName?.let {
                DetailRow("Vendor Name", it)
            }
            DetailRow("Device Type", device.deviceType.toString())
            device.dateCommissioned?.let {
                DetailRow("Date Commissioned", it.toDateString(), divider = false)
            }
//            DetailRow("Specification Version", "N/A") // TODO: Get this from the device. what is specification version??
//            DetailRow("Software Version", "N/A") // TODO: Get this from the device. what is software version??
//            DetailRow("Serial Number", "N/A") // TODO: Get this from the device. what is serial number??
//            DetailRow("Unique ID", "N/A") // TODO: Get this from the device. what is unique ID??
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
            modifier = Modifier.padding(start = 8.dp),
            textAlign = TextAlign.End,
        )
    }

    if (divider) HorizontalDivider(
        modifier = Modifier.alpha(0.3f)
    )
}

@Preview(showBackground = true)
@Composable
private fun TechnicalDetailsCardPreview() {
    TechnicalDetailsCard(DeviceTest)
}

@Preview(showBackground = true)
@Composable
fun RemoveDeviceSection(onRemove: () -> Unit = {}) {
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

@Composable
fun DeviceItemContainer(
    icon: Painter,
    title: String,
    subtitle: String,
    isOnline: Boolean = true,
    onDeviceClick: () -> Unit,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = if (isOnline) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(0.3f)
        ) else CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onDeviceClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val boxColor = if (isOnline)
                NordicSun
            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        boxColor,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isOnline)
                        MaterialTheme.colorScheme.primary else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                )

            }

            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceItemContainerPreview() {
    DeviceItemContainer(
        icon = painterResource(resource = Res.drawable.no_matter_devices),
        title = "Living Room Lamp",
        subtitle = "Dimmable Light",
        isOnline = true,
        {}
    ) {
        Text("50%", fontWeight = FontWeight.Bold)
    }

}

// -----------------------------------------------------------------------------------------------
// Constant objects used in Compose Preview
private val DeviceTest =
    Device(
        dateCommissioned = 123456789L,
        vendorId = "1234",
        productId = "5678",
        deviceType = DeviceType.LIGHT_ON_OFF,
        deviceId = DeviceId.Zero,
        name = "Living Room Light",
        productName = "My Light",
        vendorName = "Nordic Semiconductor ASA Nordic Semiconductor ASA",
        deviceMatterInfo = emptyList()

    )

private val dummyLogsForBinding = listOf(
    "Initializing secure handshake.",
    "Fetching remote server configuration.",
    "Resolving DNS for api.connection.service.",
    "Establishing TCP connection on port 443.",
    "TLS 1.3 encryption handshake successful.",
    "Authenticating user credentials.",
    "Session token generated successfully.",
    "Fetching  client configuration.",
    "Establishing the connection with local thread.",
    "TLS 1.3 encryption handshake successful.",
    "Authenticating user credentials.",
    "Session token generated successfully.",
    "Syncing fabric index of both source and target devices.",
    "Sending ACL to target device",
    "Waiting for ACL signal back from target device.",
    "Creating Binding table...",
    "Writing binding table to target device...",
    "Writing binding table to source device...",
    "Verifying the binding on both devices...",
    "Verifying data integrity checks...",
    "Connection fully established. Wrapping up..."
)