package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

object ContactSensorClusterInfo {
    const val ID: Long = 0x0045

    object Attribute {
        const val STATE_VALUE: Long = 0x0000
    }
}

class ContactSensorCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = ContactSensorClusterInfo.ID

    fun observeStateValue(): Flow<Boolean> = observeAttribute(ContactSensorClusterInfo.Attribute.STATE_VALUE)
}
