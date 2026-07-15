package no.nordicsemi.nrf.matter.controller

import no.nordicsemi.nrf.matter.model.DeviceId

interface Decommissioner {

    suspend fun decommission(deviceId: DeviceId)
}
