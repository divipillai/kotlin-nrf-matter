package no.nordicsemi.nrf.matter.home

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    val devicesUiModel by homeViewModel.devicesUiModelLiveData.observeAsState()
    val devices = devicesUiModel?.devices
    val devicesList = devices ?: emptyList()

    val onDeviceClick: (DeviceUiModel) -> Unit = remember {
        {
            // Show device details page.
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
