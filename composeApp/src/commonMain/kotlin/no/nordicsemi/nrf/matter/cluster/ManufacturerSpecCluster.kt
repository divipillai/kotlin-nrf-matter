package no.nordicsemi.nrf.matter.cluster

import no.nordicsemi.nrf.matter.model.DeviceId

class ManufacturerSpecCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = 0xFFF1FC01

    private val nameAttributeId = 0xfff10000
    private val ledAttributeId = 0xfff10001
    private val buttonAttributeId = 0xfff10002

    private val setLedCommandId = 0xFFF10000
}