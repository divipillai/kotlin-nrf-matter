package no.nordicsemi.nrf.matter.cluster

import no.nordicsemi.nrf.matter.model.DeviceId

class DoorLockCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = 0x0101

    private val lockStateAttributeId = 0x0
}
