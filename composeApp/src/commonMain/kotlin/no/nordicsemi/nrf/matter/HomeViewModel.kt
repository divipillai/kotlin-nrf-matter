package no.nordicsemi.nrf.matter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.model.Devices
import no.nordicsemi.nrf.matter.model.DevicesListUiModel
import no.nordicsemi.nrf.matter.model.DevicesState
import no.nordicsemi.nrf.matter.model.UserPreferences
import no.nordicsemi.nrf.matter.repository.DevicesRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.repository.UserPreferencesRepository

class HomeViewModel(
    devicesRepository: DevicesRepository,
    private val devicesStateRepository: DevicesStateRepository,
    userPreferencesRepository: UserPreferencesRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
): ViewModel() {
    private val devicesListUiModelFlow: Flow<DevicesListUiModel> =
        combine(
            devicesRepository.devicesFlow,
            devicesStateRepository.devicesStateFlow,
            userPreferencesRepository.userPreferencesFlow
        ) { devices, states, prefs ->
            DevicesListUiModel(
                devices = processDevices(devices, states, prefs),
                showOfflineDevices = !prefs.hideOfflineDevices
            )
        }

    val devicesUiModelFlow: StateFlow<DevicesListUiModel> =
        devicesListUiModelFlow.stateIn(
            scope,
            SharingStarted.Eagerly,
            DevicesListUiModel(emptyList(), showOfflineDevices = true)
        )

    fun updateDeviceState(deviceId: Long, isOnline: Boolean, isOn: Boolean) {
        scope.launch {
            devicesStateRepository.updateDeviceState(deviceId, isOnline = isOnline, isOn = isOn)
        }
    }

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
}

