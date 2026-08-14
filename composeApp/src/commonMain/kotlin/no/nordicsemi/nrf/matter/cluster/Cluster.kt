package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import no.nordicsemi.nrf.matter.model.DeviceId

sealed class Cluster(protected val controller: MatterClient) {

    abstract val deviceId: DeviceId
    abstract val endpoint: Int
    abstract val id: Long

    protected suspend fun <T> read(attributeId: Long): T =
        controller.readAttribute(deviceId, endpoint, id, attributeId)

    protected suspend fun <T> write(value: T, attributeId: Long) =
        controller.setAttribute(value, deviceId, endpoint, id, attributeId)

    protected suspend fun <T> observe(attributeId: Long): Flow<T> =
        controller.observeAttribute(deviceId, endpoint, id, attributeId)

    /**
     * Invokes [commandId] with [value] as its single field, or without fields when [value] is
     * `null`. The command is sent before this call returns.
     */
    protected suspend fun execute(commandId: Long, value: Any? = null) {
        // The command is sent by the call itself, the flow only carries the response.
        controller.executeCommand(value, deviceId, endpoint, id, commandId).collect()
    }
}
