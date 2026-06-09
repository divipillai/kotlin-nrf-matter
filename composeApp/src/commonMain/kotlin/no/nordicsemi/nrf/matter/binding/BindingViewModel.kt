package no.nordicsemi.nrf.matter.binding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.device.BindingUiState
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceBinding
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.repository.BindingRepository
import no.nordicsemi.nrf.matter.repository.DevicesRepository

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
data class BindingScreenState(
    val bindingUiState: BindingUiState = UiState.Idle(),
    val sourceDevices: List<Device> = emptyList(),
    val activeBindings: List<DeviceBinding> = emptyList(),
    val selectedSourceDeviceId: DeviceId? = null,
    val eligibleTargetDevices: List<Device> = emptyList(),
)

class BindingViewModel(
    private val bindingRepository: BindingRepository,
    private val devicesRepository: DevicesRepository,
    private val deviceController: DeviceController,
) : ViewModel() {

    private val _bindingScreenState = MutableStateFlow(BindingScreenState())
    val bindingScreenState: StateFlow<BindingScreenState> = _bindingScreenState.asStateFlow()

    init {
        loadSourceDevices()
        getActiveBindings()

    }

    fun updateBindingState(state: BindingUiState) =
        _bindingScreenState.update {
            it.copy(bindingUiState = state)
        }

    fun loadSourceDevices() = viewModelScope.launch {
        val bindingSourceDevices = devicesRepository.getAllDevices().devicesList.filter {
            it.deviceType == DeviceType.LIGHT_SWITCH ||
                    it.deviceType == DeviceType.OUTLET
        }
        _bindingScreenState.update {
            it.copy(sourceDevices = bindingSourceDevices)
        }
    }

    fun getActiveBindings() = viewModelScope.launch {
        bindingRepository.getAllBinding()
            .collect {
                _bindingScreenState.update { state ->
                    state.copy(activeBindings = it)
                }
            }
    }

    fun onSourceSelected(sourceDeviceId: DeviceId) {
        _bindingScreenState.update {
            it.copy(selectedSourceDeviceId = sourceDeviceId)
        }

        updateEligibleTargetDevices(sourceDeviceId)
    }

    fun initiateBinding(sourceDeviceId: DeviceId, targetDeviceId: DeviceId) =
        viewModelScope.launch {
            updateBindingState(UiState.Loading())
            bind(
                switchNodeId = sourceDeviceId,
                lightNodeId = targetDeviceId
            ).collect { state ->
                updateBindingState(state)
            }
        }

    // todo: Move it to other places like command handler.
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
                clusterId = 0x006L, // TODO: Change it to provide the cluster id based on the type of binding.
            )

            val bindingDevice = DeviceBinding(
                id = "${switchNodeId}_${lightNodeId}_${0x006L}",
                sourceNodeId = switchNodeId,
                targetNodeId = lightNodeId,
                sourceEndpoint = 1,
                targetEndpoint = 1,
                clusterId = 0x006L
            )

            bindingRepository.save(bindingDevice)

            emit(UiState.Success(bindingDevice))

        } catch (e: Exception) {
            NordicLogger.error("Binding failed: ${e.message}", e)
            emit(UiState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    fun updateEligibleTargetDevices(sourceDeviceId: DeviceId) = viewModelScope.launch {
        bindingRepository.getTargetsForDevice(sourceDeviceId)
            .collect { bindings ->
                // Filter out devices that are lights and are not already bound to the selected source device.
                val lightDevicesInRepository =
                    devicesRepository.getAllDevices().devicesList.filter {
                        it.deviceType == DeviceType.LIGHT_ON_OFF ||
                                it.deviceType == DeviceType.DIMMABLE_LIGHT
                    }
                val targetIds = bindings.map { it.targetNodeId }.toSet()

                val result = lightDevicesInRepository.filterNot { it.deviceId in targetIds }

                _bindingScreenState.update {
                    it.copy(eligibleTargetDevices = result)
                }
            }
    }
}
