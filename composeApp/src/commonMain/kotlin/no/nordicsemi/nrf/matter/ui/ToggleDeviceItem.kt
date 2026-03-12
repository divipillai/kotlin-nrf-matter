package no.nordicsemi.nrf.matter.ui

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import no.nordicsemi.nrf.matter.screens.DeviceItemContainer

@Composable
internal fun ToggleDeviceItem(
    deviceId: Long,
    title: String,
    subtitle: String,
    icon: Painter,
    enabled: Boolean,
    onToggle: (deviceId: Long, Boolean) -> Unit,
    onClick: () -> Unit
) {

    var checked by rememberSaveable { mutableStateOf(enabled) }

    DeviceItemContainer(
        icon = icon,
        title = title,
        subtitle = subtitle,
        isOnline = checked,
        onDeviceClick = onClick
    ) {
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onToggle(deviceId, checked)
            }
        )
    }
}