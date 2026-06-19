package no.nordicsemi.nrf.matter.ui.light

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.device.mapType
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

data class LightDeviceState(
    val isOn: Boolean = false,
    val brightness: Float = 0.0f
)

class LightController(
    private val device: DeviceUiModel,
    private val commandHandler: LightCommandHandler,
    private val scope: CoroutineScope,
) : MatterController {
    val lightDeviceState = MutableStateFlow(LightDeviceState())

    val ledState = MutableStateFlow<UiState<Boolean>>(UiState.Idle())
    val brightnessLevelState = MutableStateFlow<UiState<Float>>(UiState.Idle())

    init {
        observeDeviceRealtimeState()
    }

    private fun observeDeviceRealtimeState() {
        commandHandler.observeLightDeviceState(device.device)
            .onEach { state ->
                NordicLogger.info("New light device state: $state")
                (state as? UiState.Success)?.let {
                    lightDeviceState.update {
                        it.copy(isOn = state.data)
                    }
                }
            }
            .launchIn(scope)

        commandHandler.observeBrightnessState(device.device)
            .onEach { state ->
                NordicLogger.info("New brightness state: $state")
                (state as? UiState.Success)?.let {
                    lightDeviceState.update {
                        it.copy(brightness = state.data)
                    }
                }
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
            .onEach {
                NordicLogger.info("Led state $it")
                ledState.value = it.mapType { isOn }
                (it.mapType { isOn } as? UiState.Success)?.data?.let { newState ->
                    lightDeviceState.update {
                        it.copy(isOn = newState)
                    }
                }
            }
            .launchIn(scope)
    }

    fun setBrightness(device: Device, brightnessLevel: Float) {
        commandHandler.handleBrightness(device, brightnessLevel)
            .catch {
                NordicLogger.error(
                    "Failed to send Brightness level adjustment",
                    it,
                    tag = "LightController"
                )
            }
            .onEach {
                NordicLogger.info("Brightness state $it")
                brightnessLevelState.value = it.mapType { brightnessLevel }

                (it.mapType { brightnessLevel } as? UiState.Success)?.data?.let { newState ->
                    lightDeviceState.update {
                        it.copy(brightness = newState)
                    }
                }
            }
            .launchIn(scope)
    }

    @Composable
    override fun Item(onDecommission: (DeviceId) -> Unit) {
        LightItem(
            device = device,
            lightDeviceState = lightDeviceState.collectAsStateWithLifecycle().value,
            changeLightOperationState = ledState.collectAsStateWithLifecycle().value,
            changeBrightnessOperationState = brightnessLevelState.collectAsStateWithLifecycle().value,
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
