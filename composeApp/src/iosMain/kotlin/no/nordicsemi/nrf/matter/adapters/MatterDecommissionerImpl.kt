package no.nordicsemi.nrf.matter.adapters

import no.nordicsemi.nrf.matter.controller.MatterDecommissioner
import no.nordicsemi.nrf.matter.model.DeviceId

class MatterDecommissionerImpl : MatterDecommissioner {
    override suspend fun decommission(deviceId: DeviceId) {
        TODO("Not yet implemented")
    }
}