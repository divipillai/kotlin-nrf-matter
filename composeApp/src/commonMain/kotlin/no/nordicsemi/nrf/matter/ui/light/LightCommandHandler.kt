package no.nordicsemi.nrf.matter.ui.light

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.ui.CommandHandler
import kotlin.math.roundToInt

private const val ON_OFF_CLUSTER_ID: Long = 0x0006L
private const val LEVEL_CONTROL_CLUSTER_ID: Long = 0x0008L

class LightCommandHandler(
    private val deviceController: DeviceController,
) : CommandHandler {

    /**
     * Sends an On/Off command to the Matter device.
     */
    fun handleLed(
        device: Device,
        isOn: Boolean
    ) = withUiState {
        val deviceId = device.deviceId
        val endpoint = resolveEndpoint(device, clusterId = ON_OFF_CLUSTER_ID)

        deviceController.setDeviceOnOff(
            deviceId = deviceId,
            isOn = isOn,
            endpoint = endpoint,
            isDeviceOnline = true,
        )
    }

    /**
     * Sends a Brightness level command (0..100) to the Matter device.
     */
    fun handleBrightness(
        device: Device,
        brightnessLevel: Float
    ) = withUiState {
        val deviceId = device.deviceId
        val endpoint = resolveEndpoint(device, clusterId = LEVEL_CONTROL_CLUSTER_ID)

        deviceController.setBrightnessLevel(
            deviceId = deviceId,
            brightnessLevel = (1 + (brightnessLevel * 253)).roundToInt(),
            endpoint = endpoint,
        )
    }

    /**
     * Observes the real-time state of the light device, including its On/Off status and brightness level.
     */
    fun observeLightDeviceState(
        device: Device
    ): Flow<UiState<LightDeviceState>> =
        deviceController.observeLightDeviceState(
            deviceId = device.deviceId,
            endpoint = resolveEndpoint(device, clusterId = ON_OFF_CLUSTER_ID)
        ).withUiState()

}
