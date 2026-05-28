package no.nordicsemi.nrf.matter.ui.light

import androidx.compose.runtime.Composable
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.DeviceControlItem
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.light_bulb
import org.jetbrains.compose.resources.painterResource

@Composable
fun LightItem(
    device: DeviceUiModel,
    updateDeviceState: (deviceId: DeviceId, Boolean) -> Unit,
    onClick: () -> Unit
) {
    DeviceControlItem(
        deviceId = device.device.deviceId,
        title = "Light",
        subtitle = "Turn light ON or OFF",
        icon = painterResource(Res.drawable.light_bulb),
        enabled = device.isOn,
        updateDeviceState = updateDeviceState,
        onClick = onClick
    )
}
