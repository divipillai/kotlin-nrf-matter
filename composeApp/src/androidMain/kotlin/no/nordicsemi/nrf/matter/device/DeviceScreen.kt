package no.nordicsemi.nrf.matter.device

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    updateTitle: (title: String) -> Unit,
    deviceId: Long,
) {
    Log.d("AAA", "DeviceRoute deviceId [$deviceId]")

    // Launching GPS commissioning requires Activity.
    val deviceViewModel: DeviceViewModel = koinViewModel()


    // Observes values needed by the DeviceScreen.
    val deviceUiModel by deviceViewModel.deviceUiModel.collectAsState()
    Log.d("AAA", "DeviceRoute deviceUiModel [${deviceUiModel?.device?.deviceId}]")

    // TODO: Implement remove device feature.
    val lastUpdatedDeviceState by deviceViewModel.lastUpdatedDeviceState.observeAsState()

    // TODO: On/Off Switch click.
    val onOnOffClick: (value: Boolean) -> Unit = remember {
        { value ->
            deviceViewModel.updateDeviceStateOn(deviceUiModel!!, value)
        }
    }

    // TODO: Add Inspect feature. isOnline must be provided in InspectScreen because it is updated there.

    // TODO: Add Share Device feature.
    // The device sharing flow involves multiple steps as it is based on an Activity


    // FIXME
    // When app is sent to the background, and pulled back, this kicks in.

    LaunchedEffect(Unit) {
        deviceViewModel.loadDevice(deviceId)
        updateTitle("Device")

    }

    var isOnline by remember { mutableStateOf(false) }
    var isOn by remember { mutableStateOf(false) }

    if (deviceUiModel == null) {
        Text("Still loading the device information")
        return
    }
    if (deviceUiModel == null) {
        Text("Still loading the device information")
        return
    }
    val deviceState = lastUpdatedDeviceState?.devicesStateList?.find { deviceState ->
        deviceState.deviceId == deviceUiModel!!.device.deviceId
    }

    LaunchedEffect(deviceUiModel, deviceState) {

        // Device state
        deviceUiModel?.let { model ->
            isOnline =
                when (deviceState) {
                    null -> model.isOnline
                    else -> deviceState.online
                }
            isOn =
                when (deviceState) {
                    null -> model.isOn
                    else -> deviceState.on
                }
        }
        Log.d("AAA", "deviceState: isOnline [$isOnline] isOn[$isOn]")
    }
}
