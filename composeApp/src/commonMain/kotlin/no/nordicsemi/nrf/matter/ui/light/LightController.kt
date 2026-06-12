package no.nordicsemi.nrf.matter.ui.light

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

data class LightDeviceState(
    val isOn: Boolean = false,
    val brightnessPercentage: Float = 0.0f
)

class LightController(
    private val device: DeviceUiModel,
    private val commandHandler: LightCommandHandler,
    private val scope: CoroutineScope,
) : MatterController {
    val lightDeviceState = MutableStateFlow<UiState<LightDeviceState>>(UiState.Idle())

    init {
        observeDeviceRealtimeState()
    }

    private fun observeDeviceRealtimeState() {
        commandHandler.observeLightDeviceState(device.device)
            .onEach { state ->
                lightDeviceState.value = state
            }
            .launchIn(scope)
    }

    fun setLet(device: Device, isOn: Boolean) {
        commandHandler.handleLed(device, isOn)
            .catch {
                NordicLogger.error(
                    "Failed to send Brightness level adjustment",
                    it,
                    tag = "LightController"
                )
            }
            .launchIn(scope)
    }

    fun setBrightness(device: Device, brightnessLevel: Int) {
        commandHandler.handleBrightness(device, brightnessLevel)
            .catch {
                NordicLogger.error(
                    "Failed to send Brightness level adjustment",
                    it,
                    tag = "LightController"
                )
            }
            .launchIn(scope)
    }

    @Composable
    override fun Item(onDecommission: (DeviceId) -> Unit) {
        LightItem(
            device = device,
            lightDeviceState = lightDeviceState.collectAsStateWithLifecycle().value,
            onBrightnessChange = { _, brightnessLevel ->
                setBrightness(device.device, brightnessLevel)
            },
            updateDeviceState = { deviceId, state ->
                setLet(device.device, state)
            },
            onDecommission = onDecommission
        )
    }
}
