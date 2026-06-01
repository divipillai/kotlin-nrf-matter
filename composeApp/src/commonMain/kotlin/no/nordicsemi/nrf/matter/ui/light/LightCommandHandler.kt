package no.nordicsemi.nrf.matter.ui.light

import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.ui.CommandHandler

private const val ON_OFF_CLUSTER_ID: Long = 0x0006L

class LightCommandHandler (
    private val devicesStateRepository: DevicesStateRepository,
    private val deviceController: DeviceController,
) : CommandHandler {

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

            deviceController.setDeviceOnOff(
                deviceId = deviceId,
                isOn = isOn,
                endpoint = endpoint,
                isDeviceOnline = true,
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
}
