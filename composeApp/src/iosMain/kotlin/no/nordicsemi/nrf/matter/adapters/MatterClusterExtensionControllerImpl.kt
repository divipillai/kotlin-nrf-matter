package no.nordicsemi.nrf.matter.adapters

import no.nordicsemi.nrf.matter.controller.MatterClusterExtensionController
import no.nordicsemi.nrf.matter.model.DeviceId

class MatterClusterExtensionControllerImpl : MatterClusterExtensionController {

    override suspend fun generateRandomNumber(
        deviceId: DeviceId,
        endpoint: Int
    ): Long? {
        TODO("Not yet implemented")
    }
}