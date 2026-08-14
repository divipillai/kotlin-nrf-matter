package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

class OnOffCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = 0x0006

    private val onOffAttributeId = 0x0000

    suspend fun setDeviceOnOff(isOn: Boolean) {
        val value = 1.takeIf { isOn } ?: 0
        controller.setAttribute(value, deviceId, endpoint, id, onOffAttributeId)
    }

    suspend fun observeLightState(deviceId: DeviceId, endpoint: Int): Flow<Boolean> {
        return controller.observeAttribute(deviceId, endpoint, id, onOffAttributeId)
    }
}