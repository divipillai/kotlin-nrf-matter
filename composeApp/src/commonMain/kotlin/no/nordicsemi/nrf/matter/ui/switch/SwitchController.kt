package no.nordicsemi.nrf.matter.ui.switch

import androidx.compose.runtime.Composable
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

class SwitchController(
    private val device: DeviceUiModel,
)  : MatterController {

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
