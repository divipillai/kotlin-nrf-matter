package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

class DoorLockCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = ID

    /** Locks or unlocks the door. The optional PIN code field is never sent. */
    suspend fun setLocked(isLocked: Boolean) {
        execute(commandId = if (isLocked) LOCK_COMMAND_ID else UNLOCK_COMMAND_ID)
    }

    /** Emits the raw LockState value, see [no.nordicsemi.nrf.matter.model.LockDeviceState]. */
    suspend fun observeLockState(): Flow<Number> = observe(LOCK_STATE_ATTRIBUTE_ID)

    companion object {
        const val ID: Long = 0x0101

        private const val LOCK_STATE_ATTRIBUTE_ID: Long = 0x0000
        private const val LOCK_COMMAND_ID: Long = 0x00
        private const val UNLOCK_COMMAND_ID: Long = 0x01
    }
}
