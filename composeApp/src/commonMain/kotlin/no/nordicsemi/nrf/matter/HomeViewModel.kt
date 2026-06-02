package no.nordicsemi.nrf.matter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.model.Devices
import no.nordicsemi.nrf.matter.model.DevicesListUiModel
import no.nordicsemi.nrf.matter.model.DevicesState
import no.nordicsemi.nrf.matter.model.UserPreferences
import no.nordicsemi.nrf.matter.repository.DevicesRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.repository.UserPreferencesRepository
import no.nordicsemi.nrf.matter.ui.MatterController
import no.nordicsemi.nrf.matter.ui.light.LightController
import no.nordicsemi.nrf.matter.ui.lock.LockController
import no.nordicsemi.nrf.matter.ui.manspec.ManufacturerSpecController
import no.nordicsemi.nrf.matter.ui.switch.SwitchController
import org.koin.core.component.KoinComponent

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

fun DeviceUiModel.toController(scope: CoroutineScope, koin: KoinComponent): MatterController {
    return when (device.deviceType) {
        DeviceType.COLOR_TEMPERATURE_LIGHT,
        DeviceType.EXTENDED_COLOR_LIGHT,
        DeviceType.UNKNOWN -> TODO()
        DeviceType.DIMMABLE_LIGHT,
        DeviceType.LIGHT_ON_OFF -> LightController(this, koin.getKoin().get(), scope)
        DeviceType.OUTLET,
        DeviceType.LIGHT_SWITCH -> SwitchController(this, koin.getKoin().get(), scope)
        DeviceType.DOOR_LOCK -> LockController(this, koin.getKoin().get(), scope)
        DeviceType.MANUFACTURER_SPECIFIC_DEVICE -> ManufacturerSpecController(this, koin.getKoin().get(), scope)
    }
}

class HomeViewModel(
    private val devicesRepository: DevicesRepository,
    private val devicesStateRepository: DevicesStateRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel(), KoinComponent {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )
    private val devicesListUiModelFlow: Flow<DevicesListUiModel> =
        combine(
            devicesRepository.devicesFlow,
            devicesStateRepository.devicesStateFlow,
            userPreferencesRepository.userPreferencesFlow
        ) { devices, states, prefs ->
            NordicLogger.info("AAA, combine devices: $devices states: ${states.devicesStateList}")
            DevicesListUiModel(
                devices = processDevices(devices, states, prefs),
                showOfflineDevices = !prefs.hideOfflineDevices
            )
        }

    val devices: StateFlow<List<MatterController>> =
        combine(
            devicesRepository.devicesFlow,
            devicesStateRepository.devicesStateFlow,
            userPreferencesRepository.userPreferencesFlow
        ) { devices, states, prefs ->
            NordicLogger.info("AAA, combine devices: $devices states: ${states.devicesStateList}")
            DevicesListUiModel(
                devices = processDevices(devices, states, prefs),
                showOfflineDevices = !prefs.hideOfflineDevices
            ).devices.map { it.toController(scope, this) }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val devicesUiModelFlow: StateFlow<DevicesListUiModel> =
        devicesListUiModelFlow.stateIn(
            scope,
            SharingStarted.Eagerly,
            DevicesListUiModel(emptyList(), showOfflineDevices = true)
        )

    private fun processDevices(
        devices: Devices,
        devicesStates: DevicesState,
        userPreferences: UserPreferences
    ): List<DeviceUiModel> {
        val list = mutableListOf<DeviceUiModel>()
        devices.devicesList.forEach { device ->
            val state = devicesStates.devicesStateList.find { it.deviceId == device.deviceId }
            if (userPreferences.hideOfflineDevices && state?.online != true) return@forEach
            if (state == null) {
                list.add(DeviceUiModel(device, isOnline = false, isOn = false))
            } else {
                list.add(DeviceUiModel(device, state.online, state.on))
            }
        }
        return list
    }

    fun addCommissionedDevice(
        device: Device,
        isOnline: Boolean,
        isOn: Boolean,
    ) {
        scope.launch {
            devicesRepository.addDevice(device)
            devicesStateRepository.addDeviceState(
                device.deviceId,
                isOnline = isOnline,
                isOn = isOn
            )
        }
    }

    fun commissioningFailed(resultCode: Int) {
        // TODO: Handle commissioning failure with proper UI states.
        if (resultCode == 0) {
            // User simply wilfully exited from commissioning.
            return
        }
    }
}

