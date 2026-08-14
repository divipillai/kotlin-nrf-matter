package no.nordicsemi.nrf.matter.ui.device

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import no.nordicsemi.nrf.matter.composeapp.generated.resources.Res
import no.nordicsemi.nrf.matter.composeapp.generated.resources.door_lock
import no.nordicsemi.nrf.matter.composeapp.generated.resources.door_lock_open_right
import no.nordicsemi.nrf.matter.composeapp.generated.resources.light_bulb
import no.nordicsemi.nrf.matter.composeapp.generated.resources.power_settings
import no.nordicsemi.nrf.matter.composeapp.generated.resources.smart_outlet
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceType
import org.jetbrains.compose.resources.painterResource

/** The icon of the device, where [isActive] tells whether the device is on or locked. */
@Composable
fun Device.toIcon(isActive: Boolean): Painter = when (deviceType) {
    DeviceType.DOOR_LOCK -> painterResource(
        if (isActive) Res.drawable.door_lock else Res.drawable.door_lock_open_right
    )

    DeviceType.OUTLET -> painterResource(Res.drawable.smart_outlet)
    DeviceType.LIGHT_SWITCH -> painterResource(Res.drawable.power_settings)
    else -> painterResource(Res.drawable.light_bulb)
}

/** The name of the device, falling back to its type when the device is unnamed. */
fun Device.toTitle(): String = name ?: productName ?: deviceType.toString()

fun Device.toSubtitle(): String = deviceType.toString()

/** Only switches and outlets can be bound to another device, see the binding screen. */
fun Device.isBindingCapable(): Boolean =
    deviceType == DeviceType.LIGHT_SWITCH || deviceType == DeviceType.OUTLET
