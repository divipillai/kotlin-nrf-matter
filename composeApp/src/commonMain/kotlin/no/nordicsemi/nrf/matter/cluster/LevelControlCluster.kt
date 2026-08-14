package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

class LevelControlCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = 0x0006

    private val levelControlAttributeId = 0x0000

    suspend fun setDeviceOnOff(level: Int) {
        controller.setAttribute(level, deviceId, endpoint, id, levelControlAttributeId)
    }

    suspend fun observeLightState(deviceId: DeviceId, endpoint: Int): Flow<Float> {
        return controller.observeAttribute(deviceId, endpoint, id, levelControlAttributeId)
    }
}
