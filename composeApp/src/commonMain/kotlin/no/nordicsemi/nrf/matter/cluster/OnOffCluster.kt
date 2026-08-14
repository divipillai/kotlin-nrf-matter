package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

class OnOffCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = ID

    /** Turns the device on or off. The OnOff attribute itself is read only, hence the commands. */
    suspend fun setOn(isOn: Boolean) {
        execute(commandId = if (isOn) ON_COMMAND_ID else OFF_COMMAND_ID)
    }

    suspend fun observeOnOff(): Flow<Boolean> = observe(ON_OFF_ATTRIBUTE_ID)

    companion object {
        const val ID: Long = 0x0006

        private const val ON_OFF_ATTRIBUTE_ID: Long = 0x0000
        private const val OFF_COMMAND_ID: Long = 0x00
        private const val ON_COMMAND_ID: Long = 0x01
    }
}
