package no.nordicsemi.nrf.matter.chip

import no.nordicsemi.nrf.matter.controller.Decommissioner
import no.nordicsemi.nrf.matter.model.DeviceId

class DecommissionerImpl(
    private val chipClient: ChipClient,
) : Decommissioner {

    override suspend fun decommission(deviceId: DeviceId) {
        chipClient.decommissionDevice(deviceId.longValue)
    }
}
