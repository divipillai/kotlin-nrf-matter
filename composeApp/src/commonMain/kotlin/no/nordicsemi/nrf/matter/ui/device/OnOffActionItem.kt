package no.nordicsemi.nrf.matter.ui.device

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import no.nordicsemi.nrf.matter.model.DeviceId

@Composable
fun OnOffActionItem(
    deviceId: DeviceId,
    isLightOn: Boolean,
    isEnabled: Boolean,
    updateDeviceState: (DeviceId, Boolean) -> Unit,
) {
    Switch(
        checked = isLightOn,
        onCheckedChange = {
            updateDeviceState(deviceId, it)
        },
        enabled = isEnabled
    )
}
