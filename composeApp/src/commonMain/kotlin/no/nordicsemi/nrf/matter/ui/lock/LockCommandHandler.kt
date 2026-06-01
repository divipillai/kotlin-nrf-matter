package no.nordicsemi.nrf.matter.ui.lock

import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.ui.CommandHandler

private const val LOCK_UNLOCK_CLUSTER_ID: Long = 0x0101.toLong()

class LockCommandHandler(
    private val devicesStateRepository: DevicesStateRepository,
    private val deviceController: DeviceController,
) : CommandHandler {

    fun handleLock(
        device: Device,
        isLocked: Boolean
    ) = withUiState {
        val endpoint =
            resolveEndpoint(
                device,
                clusterId = LOCK_UNLOCK_CLUSTER_ID
            ) // todo: use the proper cluster id

        try {
            devicesStateRepository.updateDeviceState(
                deviceId = device.deviceId,
                isOnline = true,
                isOn = isLocked
            )

            deviceController.lockUnlockDoor(
                deviceId = device.deviceId,
                isLocked = isLocked,
                endpoint = endpoint,
            )

            isLocked
        } catch (e: Exception) {

            devicesStateRepository.updateDeviceState(
                deviceId = device.deviceId,
                isOnline = false,
                isOn = !isLocked
            )

            !isLocked
        }
    }
}
