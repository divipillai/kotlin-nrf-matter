package no.nordicsemi.nrf.matter.domain

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import no.nordicsemi.nrf.matter.device.BindingUiState
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceBinding
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.repository.BindingRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository

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
class DeviceCommandHandler(
    private val devicesStateRepository: DevicesStateRepository,
    private val deviceController: DeviceController,
    private val bindingRepository: BindingRepository,
) {
    fun handleLed(
        device: Device,
        isOn: Boolean
    ) = withUiState {
        val deviceId = device.deviceId
        val endpoint = resolveEndpoint(device, clusterId = ON_OFF_CLUSTER_ID)

        try {
            devicesStateRepository.updateDeviceState(
                deviceId = deviceId,
                isOnline = true,
                isOn = isOn
            )

            deviceController.setLed(
                deviceId = deviceId,
                isOn = isOn,
                endpoint = endpoint
            )

            isOn
        } catch (e: Exception) {

            devicesStateRepository.updateDeviceState(
                deviceId = deviceId,
                isOnline = false,
                isOn = !isOn
            )

            !isOn
        }
    }

    fun handlePower(
        device: Device,
        isOn: Boolean
    ) = withUiState {
        val endpoint = resolveEndpoint(device, clusterId = ON_OFF_CLUSTER_ID)

        try {
            devicesStateRepository.updateDeviceState(
                deviceId = device.deviceId,
                isOnline = true,
                isOn = isOn
            )

            deviceController.setDeviceOnOff(
                deviceId = device.deviceId,
                isDeviceOnline = true,
                isOn = isOn,
                endpoint = endpoint
            )

            isOn
        } catch (e: Exception) {

            devicesStateRepository.updateDeviceState(
                deviceId = device.deviceId,
                isOnline = false,
                isOn = !isOn
            )

            !isOn
        }
    }

    fun handleLock(
        device: Device,
        isLocked: Boolean
    ) = withUiState {
        val endpoint =
            resolveEndpoint(
                device,
                clusterId = LOCK_UNLOCK_CLUSTER_ID
            ) // todo: use the proper cluster id

        try {
            devicesStateRepository.updateDeviceState(
                deviceId = device.deviceId,
                isOnline = true,
                isOn = isLocked
            )

            deviceController.lockUnlockDoor(
                deviceId = device.deviceId,
                isLocked = isLocked,
                endpoint = endpoint,
            )

            isLocked
        } catch (e: Exception) {

            devicesStateRepository.updateDeviceState(
                deviceId = device.deviceId,
                isOnline = false,
                isOn = !isLocked
            )

            !isLocked
        }
    }

    private fun resolveEndpoint(device: Device, clusterId: Long): Int {
        return device.deviceMatterInfo
            .firstOrNull { it.serverClusters.contains(clusterId) }
            ?.endpoint ?: 0 // TODO: change to exception and handle from UI.
    }

    fun bind(
        switchNodeId: DeviceId,
        lightNodeId: DeviceId,
    ): Flow<BindingUiState> = flow {
        emit(UiState.Loading())

        try {
            deviceController.bind(
                sourceNodeId = switchNodeId,
                sourceEndpoint = 1, // TODO: Add a function call that looks the endpoint of the switch where binding is configured.
                targetNodeId = lightNodeId,
                targetEndpoint = 1, // TODO: Add a function call that looks the endpoint of the light where cluster id is configured.
                clusterId = ON_OFF_CLUSTER_ID, // TODO: Change it to provide the cluster id based on the type of binding.
            )

            val bindingDevice = DeviceBinding(
                id = "${switchNodeId}_${lightNodeId}_$ON_OFF_CLUSTER_ID",
                sourceNodeId = switchNodeId,
                targetNodeId = lightNodeId,
                sourceEndpoint = 1,
                targetEndpoint = 1,
                clusterId = ON_OFF_CLUSTER_ID
            )

            bindingRepository.save(bindingDevice)

            emit(UiState.Success(bindingDevice))

        } catch (e: Exception) {
            Napier.e(e) { "Binding failed: ${e.message}" }
            emit(UiState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    fun subscribeToButtonChanges(deviceId: DeviceId): Flow<UiState<Boolean>> {
        return deviceController.subscribeToButtonChanges(deviceId, 1).withUiState()
    }

    fun generateRandomNumber(deviceId: DeviceId): Flow<UiState<Int>> {
        return withUiState {
            deviceController.generateRandomNumber(deviceId)
        }
    }

    private fun <T> Flow<T>.withUiState(): Flow<UiState<T>> {
        return this
            .map<T, UiState<T>> { UiState.Success(it) }
            .onStart { emit(UiState.Loading()) }
            .catch { emit(UiState.Error("Error during executing operation.", it)) }
    }

    private fun <T> withUiState(block: suspend () -> T): Flow<UiState<T>> {
        return flow {
            try {
                emit(UiState.Loading())
                emit(UiState.Success(block()))
            } catch (t: Throwable) {
                t.printStackTrace()
                emit(UiState.Error("Error during executing operation.", t))
            }
        }
    }

    companion object {
        private val TAG: String
            get() = "DeviceCommandHandler"
        private const val ON_OFF_CLUSTER_ID: Long = 0x0006L
        private const val LOCK_UNLOCK_CLUSTER_ID: Long = 0x0101.toLong()
        private const val MANUFACTURER_SPECIFIC_CLUSTER_ID: Long = 0xFFF1FC01
    }
}
