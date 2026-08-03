@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.controller.MatterDoorLockController
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.LockDeviceState
import iosMatter.LocalMatterDoorController

class MatterDoorLockControllerImpl : MatterDoorLockController {

    private val controller = LocalMatterDoorController()

    override suspend fun lockUnlockDoor(
        deviceId: DeviceId,
        isLocked: Boolean,
        endpoint: Int
    ) {
        return suspendCancellableCoroutine { continuation ->
            controller.lockUnlockDoorWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                isLocked = isLocked,
                endpoint = endpoint.toNSNumber()
            ) { error ->
                continuation.handleResult(error)
            }
        }
    }

    override suspend fun observeLockState(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<LockDeviceState> {
        return callbackFlow {
            controller.observeLockStateWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                endpoint = endpoint.toNSNumber(),
                onUpdate = { trySend(LockDeviceState.create(it)) }
            ) { error ->
                error?.let { close(IOSException(it)) }
            }

            awaitClose {  }
        }
    }
}
