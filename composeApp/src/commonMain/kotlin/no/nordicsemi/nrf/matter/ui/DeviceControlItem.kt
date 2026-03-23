package no.nordicsemi.nrf.matter.ui

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.screens.DeviceItemContainer

@Composable
internal fun DeviceControlItem(
    deviceId: DeviceId,
    title: String,
    subtitle: String,
    icon: Painter,
    enabled: Boolean,
    updateDeviceState: (deviceId: DeviceId, Boolean) -> Unit,
    onClick: () -> Unit
) {
    DeviceItemContainer(
        icon = icon,
        title = title,
        subtitle = subtitle,
        isOnline = enabled,
        onDeviceClick = onClick
    ) {
        Switch(
            checked = enabled,
            onCheckedChange = {
                updateDeviceState(deviceId, it)
            }
        )
    }
}