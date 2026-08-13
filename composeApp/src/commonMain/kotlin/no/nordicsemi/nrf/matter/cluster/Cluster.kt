package no.nordicsemi.nrf.matter.cluster

import no.nordicsemi.nrf.matter.model.DeviceId

abstract class Cluster(protected val controller: MatterClient) {

    abstract val deviceId: DeviceId
    abstract val endpoint: Int
    abstract val id: Int
}
