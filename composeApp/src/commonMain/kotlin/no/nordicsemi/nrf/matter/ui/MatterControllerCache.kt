package no.nordicsemi.nrf.matter.ui

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.light.LightController
import no.nordicsemi.nrf.matter.ui.lock.LockController
import no.nordicsemi.nrf.matter.ui.manspec.ManufacturerSpecController
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
            DeviceType.DIMMABLE_LIGHT,
            DeviceType.LIGHT_ON_OFF -> LightController(device, get(), scope)
            DeviceType.OUTLET,
            DeviceType.LIGHT_SWITCH -> SwitchController(device)
            DeviceType.DOOR_LOCK -> LockController(device, get(), scope)
            DeviceType.MANUFACTURER_SPECIFIC_DEVICE -> ManufacturerSpecController(device, get(), scope)
        }.also {
            controllerCache[device.device.deviceId] = it
        }
    }
}
