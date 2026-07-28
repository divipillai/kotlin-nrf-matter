package no.nordicsemi.nrf.matter.adapters

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.controller.MatterDoorLockController
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.LockDeviceState

class MatterDoorLockControllerImpl : MatterDoorLockController {
    override suspend fun lockUnlockDoor(
        deviceId: DeviceId,
        isLocked: Boolean,
        endpoint: Int
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun observeLockState(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<LockDeviceState> {
        TODO("Not yet implemented")
    }
}