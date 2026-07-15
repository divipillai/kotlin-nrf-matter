package no.nordicsemi.nrf.matter.controller

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.LockDeviceState

interface MatterDoorLockController {

    /**
     * Locks or unlocks the door via the Door Lock cluster.
     *
     * @param deviceId the commissioned device to control.
     * @param isLocked `true` to send the Lock Door command, `false` to send the Unlock Door command.
     * @param endpoint the Matter endpoint exposing the Door Lock cluster.
     * @param pinCode optional PIN code required by the lock to authorize the operation; when
     * omitted, an empty PIN is sent.
     * @throws Exception if the underlying cluster command fails (e.g. device unreachable, command
     * rejected).
     */
    suspend fun lockUnlockDoor(
        deviceId: DeviceId,
        isLocked: Boolean,
        endpoint: Int,
        pinCode: String? = null,
    )

    /**
     * Subscribes to the LockState attribute of a door lock endpoint and emits its state as it
     * changes.
     *
     * The subscription reports changes instantly and otherwise sends a heartbeat every 10 seconds;
     * establishing the underlying session is subject to a 10 second timeout. The returned [Flow]
     * closes with an exception if the subscription cannot be established.
     *
     * @param deviceId the commissioned device to observe.
     * @param endpoint the Matter endpoint exposing the Door Lock cluster.
     * @param doorLockClusterId the Door Lock cluster ID reported by this device (typically 257L).
     * @return a cold [Flow] emitting the current [LockDeviceState].
     */
    fun observeLockState(
        deviceId: DeviceId,
        endpoint: Int,
        doorLockClusterId: Long,
    ): Flow<LockDeviceState>
}
