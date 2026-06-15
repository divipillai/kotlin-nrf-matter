package no.nordicsemi.nrf.matter.ui.manspec

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.ui.CommandHandler

private const val BASIC_INFORMATION_CLUSTER_ID: Long = 0x28
private const val MANUFACTURER_SPECIFIC_CLUSTER_ID: Long = 0xFFF1FC01

class ManufacturerSpecCommandHandler(
    private val devicesStateRepository: DevicesStateRepository,
    private val deviceController: DeviceController,
) : CommandHandler {

    fun handleLed(
        device: Device,
        isOn: Boolean
    ) = withUiState {
        val deviceId = device.deviceId
        val endpoint = resolveEndpoint(device, clusterId = MANUFACTURER_SPECIFIC_CLUSTER_ID)

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

    fun subscribeToButtonChanges(device: Device): Flow<UiState<Boolean>> {
        val endpoint = resolveEndpoint(device, MANUFACTURER_SPECIFIC_CLUSTER_ID)
        return deviceController.subscribeToButtonChanges(device.deviceId, endpoint).withUiState()
    }

    fun generateRandomNumber(device: Device): Flow<UiState<Int>> {
        return withUiState {
            val endpoint = resolveEndpoint(device, BASIC_INFORMATION_CLUSTER_ID)
            deviceController.generateRandomNumber(device.deviceId, endpoint)
        }
    }
}
