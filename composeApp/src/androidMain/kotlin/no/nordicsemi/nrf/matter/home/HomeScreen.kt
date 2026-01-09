package no.nordicsemi.nrf.matter.home

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningRequest
import no.nordicsemi.nrf.matter.service.AppCommissioningService
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

@Composable
internal fun HomeRoute(
    innerPaddings: PaddingValues,
    updateTitle: (title: String) -> Unit,
    navigateToDevice: (deviceId: Long) -> Unit,
    onCommissionDevice: () -> Unit,
) {
    LaunchedEffect(Unit) {
        updateTitle("Home")
    }
    HomeScreen(
        innerPaddings,
        navigateToDevice,
        onCommissionDevice = onCommissionDevice
    )
}

@Composable
private fun HomeScreen(
    innerPaddings: PaddingValues,
    navigateToDevice: (deviceId: Long) -> Unit,
    onCommissionDevice: () -> Unit,
) {
    val homeViewModel: HomeViewModel = koinViewModel()

    // Controls when the "New Device" alert dialog is shown.
    // When that alert dialog completes, control needs to go back to the ViewModel to complete
    // the commissioning flow.
    val showNewDeviceAlertDialog by homeViewModel.showNewDeviceNameAlertDialog.collectAsState()
    val onCommissionedDeviceNameCaptured: (name: String) -> Unit = remember {
        {
            homeViewModel.onCommissionedDeviceNameCaptured(it)
        }
    }

    // UI Model for all the devices shown on the screen.
    val devicesUiModel by homeViewModel.devicesUiModelLiveData.observeAsState()
    val devices = devicesUiModel?.devices
    val devicesList = devices ?: emptyList()

    // Functions invoked when UI controls are clicked on a specific device in the list.
    val onDeviceClick: (DeviceUiModel) -> Unit = remember {
        {
            // Show device details page.
            // TODO: Navigate to device details page.
            Log.d("AAA", "onDeviceClick: ${it.device.name}, id: ${it.device.deviceId}")
            // todo: show device detail in the
            navigateToDevice(it.device.deviceId)
        }
    }
    val onOnOffClick: (deviceId: Long, value: Boolean) -> Unit = remember {
        { deviceId, value ->
            homeViewModel.updateDeviceStateOn(deviceId, value)
        }
    }
    Box(modifier = Modifier.padding(innerPaddings)) {
        if (devicesList.isEmpty()) {
            // Pass a callback to simulate adding a device for testing
            NoDevicesScreen(
                onAddDeviceClick = { onCommissionDevice() }
            )
        } else {
            DeviceList(
                devicesList,
                onDeviceClick = onDeviceClick,
                onOnOffClick = onOnOffClick
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    showNewDeviceAlertDialog: Boolean,
    devicesList: List<DeviceUiModel>,
    onCommissionedDeviceNameCaptured: (name: String) -> Unit,
    onCommissionDevice: () -> Unit,
    onDeviceClick: (DeviceUiModel) -> Unit,
    onOnOffClick: (deviceId: Long, value: Boolean) -> Unit
) {
    // Alert Dialog shown when the name of the device must be captured in the commissioning flow.
    NewDeviceAlertDialog(
        showNewDeviceAlertDialog,
        onCommissionedDeviceNameCaptured,
    )

    // Content for the screen.
    Box {
        if (devicesList.isEmpty()) {
            NoDevices()
        } else {
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    // verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    this.items(devicesList) { device ->
                        val onDeviceItemClick: () -> Unit = { onDeviceClick(device) }
                        DeviceItem(
                            device.device.deviceId,
//                            device.device.deviceType, // TODO: Add device type later.
                            device.device.name,
                            device.isOnline,
                            device.isOn,
                            onOnOffClick,
                            onDeviceItemClick,
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onCommissionDevice,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
        }
    }
    LaunchedEffect(devicesList) { Log.d("AAA", "HomeRoute [$devicesList]") }
}

/**
 * Commission a device.
 */
fun commissionDevice(
    context: Context,
    commissionDeviceLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
) {
    val commissionDeviceRequest =
        CommissioningRequest.builder()
//            .setOnboardingPayload(payload) // Add device payload directly to commission a specific device, such as payload = "MT:6FCJ142C00KA0648G00"
            .setCommissioningService(ComponentName(context, AppCommissioningService::class.java))
            .build()

    Matter.getCommissioningClient(context)
        .commissionDevice(commissionDeviceRequest)
        .addOnSuccessListener { result ->
            commissionDeviceLauncher.launch(IntentSenderRequest.Builder(result).build())
        }
        .addOnFailureListener { error ->
            Log.e("AAA", error.message.toString())
        }
}

@Composable
private fun NewDeviceAlertDialog(
    showNewDeviceAlertDialog: Boolean,
    onCommissionedDeviceNameCaptured: (name: String) -> Unit,
) {
    if (!showNewDeviceAlertDialog) {
        return
    }

    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        title = { Text(text = "Specify device name") },
        text = {
            Column {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Device name") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Process inputText
                    onCommissionedDeviceNameCaptured(inputText)
                },
                enabled = inputText.isNotEmpty(),
            ) {
                Text("OK")
            }
        },
        onDismissRequest = {},
        dismissButton = {},
    )
}

@Composable
private fun NoDevices() {
    Column(
        modifier = Modifier.fillMaxSize(), // Make the Column occupy the whole screen
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = Icons.Default.Warning,
            contentDescription = "No device",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
        Text(
            text = "No device yet",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
        )
        Text(
            text = "Add your first device to get started",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun DeviceItem(
    deviceId: Long,
//    deviceType: DeviceType, // TODO: Add device type later.
    name: String?,
    isOnline: Boolean,
    isOn: Boolean,
    onOnOffClick: (deviceId: Long, value: Boolean) -> Unit,
    onDeviceClick: () -> Unit,
) {
    val bgColor =
        if (isOnline && isOn) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.surface
    val contentColor =
        if (isOnline && isOn) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface
    val text = stateDisplayString(isOnline, isOn)
//    val iconId = getDeviceTypeIconId(deviceType)
    val onCheckedChange: (value: Boolean) -> Unit = { onOnOffClick(deviceId, it) }

    Surface(
        modifier = Modifier
            .padding(top = 12.dp)
            .padding(PaddingValues(horizontal = 12.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        contentColor = contentColor,
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        onClick = onDeviceClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // TODO: ADD icons later
//            Icon(
//                painter = painterResource(id = iconId),
//                contentDescription = null, // decorative element
//            )
            Column {
                Text(text = name ?: "No Name/Unknown", style = MaterialTheme.typography.bodyLarge)
                Text(text = text, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.weight(1f))
            Switch(checked = isOn, onCheckedChange = onCheckedChange)
        }
    }
}

/** Converts the combo of "isOnline" and "isOn" into a proper string for the UI. */
fun stateDisplayString(isOnline: Boolean, isOn: Boolean): String {
    return if (!isOnline) {
        "OFFLINE"
    } else {
        if (isOn) "ON" else "OFF"
    }
}