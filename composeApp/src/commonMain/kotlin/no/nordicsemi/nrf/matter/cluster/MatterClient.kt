package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

abstract class MatterClient {

    abstract suspend fun <T> setAttribute(value: T, deviceId: DeviceId, endpoint: Int, clusterId: Int, attributeId: Int)

    abstract suspend fun <T> readAttribute(deviceId: DeviceId, endpoint: Int, clusterId: Int, attributeId: Int): T

    abstract suspend fun <T> observeAttribute(deviceId: DeviceId, endpoint: Int, clusterId: Int, attributeId: Int): Flow<T>

    abstract suspend fun <T> executeCommand(value: T, deviceId: DeviceId, endpoint: Int, clusterId: Int, commandId: Int): Flow<T>
}
