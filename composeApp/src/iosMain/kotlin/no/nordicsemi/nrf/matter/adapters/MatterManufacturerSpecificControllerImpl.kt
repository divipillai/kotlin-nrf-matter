package no.nordicsemi.nrf.matter.adapters

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.controller.MatterManufacturerSpecificController
import no.nordicsemi.nrf.matter.model.DeviceId

class MatterManufacturerSpecificControllerImpl : MatterManufacturerSpecificController {
    override suspend fun setLed(
        deviceId: DeviceId,
        isOn: Boolean,
        endpoint: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun observeButtonChanges(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Boolean> {
        TODO("Not yet implemented")
    }
}