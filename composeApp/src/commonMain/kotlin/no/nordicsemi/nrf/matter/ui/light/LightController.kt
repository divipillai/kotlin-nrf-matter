package no.nordicsemi.nrf.matter.ui.light

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.time.Duration.Companion.milliseconds

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
            .delaySuccess()
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
            .delaySuccess()
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

    // A dk can go to a strange state when there are too many requests at once.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> Flow<UiState<T>>.delaySuccess(): Flow<UiState<T>> {
        return flatMapConcat { state ->
            when (state) {
                is UiState.Success -> flow {
                    delay(300.milliseconds)
                    emit(state)
                }
                else -> flowOf(state)
            }
        }
    }

    @Composable
    override fun Item(onDecommission: (DeviceId) -> Unit) {
        val ledRequestState = ledState.collectAsStateWithLifecycle().value
        val brightnessRequestState = brightnessLevelState.collectAsStateWithLifecycle().value
        val isEnabled = ledRequestState !is UiState.Loading && brightnessRequestState !is UiState.Loading

        LightItem(
            device = device,
            lightDeviceState = lightDeviceState.collectAsStateWithLifecycle().value,
            isEnabled = isEnabled,
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
