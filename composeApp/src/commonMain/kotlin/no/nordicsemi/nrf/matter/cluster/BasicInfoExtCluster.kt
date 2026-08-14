package no.nordicsemi.nrf.matter.cluster

import no.nordicsemi.nrf.matter.model.DeviceId

class BasicInfoExtCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = 0x28

    private val randomNumberAttributeId = 0x17
    private val generateRandomNumberCommandId = 0x00
}