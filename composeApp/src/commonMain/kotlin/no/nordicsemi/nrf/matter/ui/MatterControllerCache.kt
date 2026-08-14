package no.nordicsemi.nrf.matter.ui

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.device.DeviceViewModel
import no.nordicsemi.nrf.matter.ui.switch.SwitchController
import no.nordicsemi.nrf.matter.ui.unsupported.UnsupportedController
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MatterControllerCache(
    private val scope: CoroutineScope,
): KoinComponent {

    private val controllerCache = mutableMapOf<DeviceId, MatterController>()

    operator fun get(id: DeviceId): MatterController? {
        return controllerCache[id]
    }

    fun create(device: DeviceUiModel): MatterController {
        return when (device.device.deviceType) {
            DeviceType.COLOR_TEMPERATURE_LIGHT,
            DeviceType.EXTENDED_COLOR_LIGHT,
            DeviceType.UNSUPPORTED -> UnsupportedController(device)
            DeviceType.OUTLET,
            DeviceType.LIGHT_SWITCH -> SwitchController(device)
            // Devices which are controlled through their clusters.
            DeviceType.DIMMABLE_LIGHT,
            DeviceType.LIGHT_ON_OFF,
            DeviceType.DOOR_LOCK,
            DeviceType.MANUFACTURER_SPECIFIC_DEVICE -> DeviceViewModel(device, get(), scope)
        }.also {
            controllerCache[device.device.deviceId] = it
        }
    }
}
