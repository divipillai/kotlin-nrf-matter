package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.chip.ChipClient
import no.nordicsemi.nrf.matter.model.DeviceId

class AndroidMatterClient(
    private val chipClient: ChipClient
): MatterClient() {

    override suspend fun <T> setAttribute(
        value: T,
        deviceId: DeviceId,
        endpoint: Int,
        clusterId: Int,
        attributeId: Int
    ) {
        val devicePointer = chipClient.getConnectedDevicePointer(deviceId.longValue)
        chipClient.readAttribute(devicePointer, endpoint, clusterId, attributeId)
    }

    override suspend fun <T> readAttribute(
        deviceId: DeviceId,
        endpoint: Int,
        clusterId: Int,
        attributeId: Int
    ): T {
        val devicePointer = chipClient.getConnectedDevicePointer(deviceId.longValue)
        return chipClient.readAttribute(devicePointer, endpoint, clusterId, attributeId) as T
    }

    override suspend fun <T> observeAttribute(
        deviceId: DeviceId,
        endpoint: Int,
        clusterId: Int,
        attributeId: Int
    ): Flow<T> {
        TODO("Not yet implemented")
    }

    override suspend fun <T> executeCommand(
        value: T,
        deviceId: DeviceId,
        endpoint: Int,
        clusterId: Int,
        commandId: Int
    ): Flow<T> {
        TODO("Not yet implemented")
    }
}