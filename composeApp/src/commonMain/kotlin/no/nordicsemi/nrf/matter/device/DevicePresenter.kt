package no.nordicsemi.nrf.matter.device

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.binding.BindingUiStates
import no.nordicsemi.nrf.matter.domain.DeviceCommandHandler
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.repository.DevicesRepository
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

class DevicePresenter(
    private val devicesRepository: DevicesRepository,
    private val devicesStateRepository: DevicesStateRepository,
    private val deviceController: DeviceController,
    private val deviceCommandHandler: DeviceCommandHandler
) {
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private val _bindingState = MutableStateFlow<BindingUiStates>(BindingUiStates.Idle)
    val bindingState: StateFlow<BindingUiStates> = _bindingState.asStateFlow()

    fun observeDevice(deviceId: DeviceId) {
        scope.launch {
            devicesStateRepository.devicesStateFlow
                .map { states ->
                    val state = states.devicesStateList.find { it.deviceId == deviceId }
                    state?.on ?: false
                }
                .distinctUntilChanged() // optional
                .collect { isOn ->
                    val device = devicesRepository.getDeviceOrNull(deviceId) ?: return@collect
                    _uiState.update {
                        it.copy(
                            deviceUiModel = DeviceUiModel(
                                device = device,
                                isOnline = true, // or from state
                                isOn = isOn
                            )
                        )
                    }
                }
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Remove device

    // Removes the device. First we remove the fabric from the device, and then we remove the
    // device from the app's devices repository.
    // Note that unlinking the device may take a while if the device is offline.
    // If removing the fabric from the device fails (e.g. device is offline), then a dialog
    // is shown so the user has the option to force remove the device without unlinking
    // the fabric at the device. If a forced removal is selected, then function
    // removeDeviceWithoutUnlink is called.
    fun removeDevice(deviceId: DeviceId) {
        _uiState.update {
            it.copy(removeDeviceState = RemoveDeviceState.Removing)
        }

        scope.launch {
            try {
                deviceController.unlinkDevice(deviceId)
            } catch (e: Exception) {
                Napier.e(e) { "Error unlinking device: ${e.message}" }
                // Error on removing device. Show error dialog with an option to force remove.
                updateRemoveDeviceState(RemoveDeviceState.ForceRemove(deviceId))
                return@launch
            }
            try {
                devicesRepository.removeDevice(deviceId)
                updateRemoveDeviceState(RemoveDeviceState.Removed(deviceId))
            } catch (e: Exception) {
                Napier.e(e) { "Error removing device: ${e.message}" }
            }

        }
    }

    fun updateRemoveDeviceState(state: RemoveDeviceState) {
        _uiState.update { it.copy(removeDeviceState = state) }
    }

    // Removes the device from the app's devices repository, and does not unlink the fabric
    // from the device.
    // This function is called after removeDevice() has failed trying to unlink the device
    // and the user has confirmed that the device should still be removed from the app's device
    // repository.
    fun removeDeviceWithoutUnlink(deviceId: DeviceId) {
        scope.launch {
            try {
                // Remove device from the app's devices repository.
                devicesRepository.removeDevice(deviceId)
                // Notify UI so we navigate back to Home screen.
                updateRemoveDeviceState(RemoveDeviceState.Removed(deviceId))
            } catch (e: Exception) {
                Napier.e(e) { "Error removing device: ${e.message}" }
            }

        }
    }

    fun togglePower(deviceId: DeviceId, isOn: Boolean) {
        try {
            scope.launch {
                devicesStateRepository.updateDeviceState(deviceId, true, isOn)
                deviceCommandHandler.execute(deviceId, isOn)
            }
        } catch (e: Exception) {
            // revert or show error
            Napier.e { "Error toggling power: ${e.message}" }
        }
    }


    // Method to call binding.
    fun initiateBinding(switchNodeId: DeviceId) {
        scope.launch {
            _bindingState.value = BindingUiStates.InProgress

            try {
                val lightNodeId = devicesRepository.getAllDevices().devicesList.find {
                    it.deviceType == DeviceType.LIGHT_ON_OFF ||
                            it.deviceType == DeviceType.DIMMABLE_LIGHT
                }?.deviceId

                if (lightNodeId == null) {
                    _bindingState.value = BindingUiStates.Error("No light found")
                    return@launch
                }

                val binding = deviceCommandHandler.bind(
                    switchNodeId = switchNodeId,
                    lightNodeId = lightNodeId
                )

                _bindingState.value = BindingUiStates.Success(binding)

                // Todo: Persist binding to the repository
                // bindingRepository.save(binding)

            } catch (e: Exception) {
                Napier.e(e) { "Binding failed: ${e.message}" }
                _bindingState.value = BindingUiStates.Error(e.message ?: "Unknown error")
            }
        }
    }
}
