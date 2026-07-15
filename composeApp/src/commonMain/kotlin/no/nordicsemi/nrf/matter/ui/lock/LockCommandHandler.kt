package no.nordicsemi.nrf.matter.ui.lock

import no.nordicsemi.nrf.matter.controller.MatterDoorLockController
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.ui.CommandHandler

private const val LOCK_UNLOCK_CLUSTER_ID: Long = 0x0101.toLong()

class LockCommandHandler(
    private val deviceController: MatterDoorLockController,
) : CommandHandler {

    /**
     * Sends a Lock or Unlock command to the Matter device.
     */
    fun handleLock(
        device: Device,
        isLocked: Boolean
    ) = withUiState {
        val endpoint = resolveEndpoint(
            device,
            clusterId = LOCK_UNLOCK_CLUSTER_ID
        )

        deviceController.lockUnlockDoor(
            deviceId = device.deviceId,
            isLocked = isLocked,
            endpoint = endpoint,
        )

        NordicLogger.debug(
            "Door lock command sent (Should Lock: $isLocked) for device ${device.deviceId}",
            tag = "LockCommandHandler"
        )
    }

    fun observeLockDeviceState(
        device: Device
    ) =
        deviceController.observeLockState(
            deviceId = device.deviceId,
            endpoint = resolveEndpoint(device, clusterId = LOCK_UNLOCK_CLUSTER_ID),
            doorLockClusterId = LOCK_UNLOCK_CLUSTER_ID
        )
}
