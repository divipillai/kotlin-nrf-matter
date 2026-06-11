package no.nordicsemi.nrf.matter.ui.light

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

class LightController(
    private val device: DeviceUiModel,
    private val commandHandler: LightCommandHandler,
    private val scope: CoroutineScope,
)  : MatterController {

    val ledState = MutableStateFlow<UiState<Boolean>>(UiState.Idle())

    fun setLet(device: Device, isOn: Boolean) {
        commandHandler.handleLed(device, isOn)
            .onEach { ledState.value = it }
            .launchIn(scope)
    }

    @Composable
    override fun Item(onDecommission: (DeviceId) -> Unit) {
        LightItem(
            device = device,
            isLedOn = ledState.collectAsStateWithLifecycle().value,
            updateDeviceState = { deviceId, state ->
                setLet(device.device, state)
            },
            onDecommission = onDecommission
        )
    }
}
