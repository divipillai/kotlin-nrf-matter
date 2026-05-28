package no.nordicsemi.nrf.matter.ui.light

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.domain.DeviceCommandHandler
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

class LightController(
    private val device: DeviceUiModel,
    private val deviceCommandHandler: DeviceCommandHandler,
    private val scope: CoroutineScope,
)  : MatterController {

    val ledState = MutableStateFlow<UiState<Boolean>>(UiState.Idle())

    fun setLet(device: Device, isOn: Boolean) {
        deviceCommandHandler.handleLed(device, isOn)
            .onEach { ledState.value = it }
            .launchIn(scope)
    }

    @Composable
    override fun Item(onDeviceClick: (DeviceId) -> Unit) {
        LightItem(
            device = device,
            updateDeviceState = { deviceId, state -> setLet(device.device, state) },
            onClick = { onDeviceClick(device.device.deviceId) },
        )
    }
}
