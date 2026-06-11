package no.nordicsemi.nrf.matter.ui.switch

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

class SwitchController(
    private val device: DeviceUiModel,
    private val commandHandler: SwitchCommandHandler,
    private val scope: CoroutineScope,
)  : MatterController {

    val powerState = MutableStateFlow<UiState<Boolean>>(UiState.Idle())

    fun setPower(device: Device, isOn: Boolean) {
        commandHandler.handleOutlet(device, isOn)
            .onEach { powerState.value = it }
            .launchIn(scope)
    }

    @Composable
    override fun Item(onDecommission: (DeviceId) -> Unit) {
        SwitchItem(
            device = device,
            title = "Light Switch",
            subtitle = "Bind the switch with other devices",
            onDecommission = onDecommission,
        )
    }
}
