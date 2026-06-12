package no.nordicsemi.nrf.matter.ui.light

import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.ui.CommandHandler
import kotlin.math.roundToInt

private const val ON_OFF_CLUSTER_ID: Long = 0x0006L
private const val LEVEL_CONTROL_CLUSTER_ID: Long = 0x0008L

class LightCommandHandler(
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

    // TODO: Implement brightness control
    fun handleBrightness(
        device: Device,
        brightnessLevel: Int
    ) = withUiState {
        val deviceId = device.deviceId
        val endpoint = resolveEndpoint(device, clusterId = LEVEL_CONTROL_CLUSTER_ID)

        try {
            deviceController.setBrightnessLevel(
                deviceId = deviceId,
                brightnessLevel = brightnessLevel,
                endpoint = endpoint,
            )
            NordicLogger.debug(
                "Brightness level set to $brightnessLevel for device $deviceId",
                tag = "LightCommandHandler"
            )
            brightnessLevel
        } catch (e: Exception) {
            NordicLogger.error("Failed to set brightness level for device $deviceId", e)
            1// todo: Return a previous brightness level in case of failure with an appropriate error handling strategy.
        }
    }
}


fun Float.toMatterBrightness(): Int {
    // Maps 0.0f..1.0f directly to 1..254
    return (1 + (this * 253)).roundToInt()
}