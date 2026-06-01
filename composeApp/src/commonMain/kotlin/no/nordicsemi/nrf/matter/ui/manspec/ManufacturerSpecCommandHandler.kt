package no.nordicsemi.nrf.matter.ui.manspec

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.ui.CommandHandler

private const val CLUSTER_ID: Long = 0xFFF1FC01

class ManufacturerSpecCommandHandler(
    private val devicesStateRepository: DevicesStateRepository,
    private val deviceController: DeviceController,
) : CommandHandler {

    fun handleLed(
        device: Device,
        isOn: Boolean
    ) = withUiState {
        val deviceId = device.deviceId
        val endpoint = resolveEndpoint(device, clusterId = CLUSTER_ID)

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

    fun subscribeToButtonChanges(deviceId: DeviceId): Flow<UiState<Boolean>> {
        return deviceController.subscribeToButtonChanges(deviceId, 1).withUiState()
    }

    fun generateRandomNumber(deviceId: DeviceId): Flow<UiState<Int>> {
        return withUiState {
            deviceController.generateRandomNumber(deviceId)
        }
    }
}
