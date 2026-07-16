package no.nordicsemi.nrf.matter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.cloudy.cloudy
import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.showToast
import no.nordicsemi.nrf.matter.HomeViewModel
import no.nordicsemi.nrf.matter.commission.DecommissionState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.model.toDeviceId

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
internal fun DeviceList(
    homeViewModel: HomeViewModel
) {
    val decommissionState by homeViewModel.decommissionState.collectAsStateWithLifecycle()
    val devices by homeViewModel.devices.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .then(if (decommissionState is DecommissionState.InProgress) Modifier.cloudy() else Modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        devices.forEach {
            item {
                when (val state = decommissionState) {
                    is DecommissionState.Error -> {
                        // Show error dialog with an option to force remove.
                        AlertDialogView(
                            onDismiss = {
                                homeViewModel.updateDecommissionState(DecommissionState.Idle)
                            },
                            onConfirm = {
                                homeViewModel.updateDecommissionState(
                                    DecommissionState.ForceRemove(
                                        state.deviceId
                                    )
                                )
                            },
                            title = "Error Removing Device",
                            message = "An error occurred while removing the device. Force remove?"
                        )
                    }

                    is DecommissionState.ForceRemove -> {
                        homeViewModel.forceRemove(state.deviceId)
                    }

                    DecommissionState.Idle -> {
                        // DO NOTHING
                    }

                    is DecommissionState.InProgress -> {
                        // Show loader while the device is being removed.
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

                    is DecommissionState.Success -> {
                        homeViewModel.updateDecommissionState(DecommissionState.Idle)
                        showToast(
                            message = "Device decommissioned successfully!",
                            duration = ToastDuration.Long,
                            gravity = ToastGravity.Center
                        )
                    }
                }

                it.Item { deviceId -> homeViewModel.decommissionDevice(deviceId) }
            }
        }
    }
}

internal val DeviceTest_LIGHT =
    Device(
        dateCommissioned = 123456789L,
        vendorId = "1234",
        productId = "5678",
        deviceType = DeviceType.LIGHT_ON_OFF,
        deviceId = 1L.toDeviceId(),
        name = "Living Room Light",
        productName = "My Light",
        vendorName = "MyVendor",
        deviceMatterInfo = emptyList()
    )

internal val DeviceTest_DOORLOCK =
    Device(
        dateCommissioned = 123456789L,
        vendorId = "1234",
        productId = "5678",
        deviceType = DeviceType.DOOR_LOCK,
        deviceId = 2L.toDeviceId(), // Fix: Changed deviceId to 2L to ensure unique keys in LazyColumn
        name = "Front Door", // Updated name for consistency
        productName = "My Lock",
        vendorName = "MyVendor",
        deviceMatterInfo = emptyList()
    )

internal val TestDeviceLockDoor = DeviceUiModel(
    device = DeviceTest_DOORLOCK,
    isOnline = true,
    isOn = true
)

internal val TestDeviceLight = DeviceUiModel(
    device = DeviceTest_LIGHT,
    isOnline = true,
    isOn = true
)
