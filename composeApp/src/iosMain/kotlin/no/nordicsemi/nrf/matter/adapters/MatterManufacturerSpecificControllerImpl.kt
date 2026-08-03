@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.controller.MatterManufacturerSpecificController
import no.nordicsemi.nrf.matter.model.DeviceId
import iosMatter.LocalMatterCustomClusterController

class MatterManufacturerSpecificControllerImpl : MatterManufacturerSpecificController {

    private val controller = LocalMatterCustomClusterController()

    override suspend fun setLed(
        deviceId: DeviceId,
        isOn: Boolean,
        endpoint: Int
    ) {
        return suspendCancellableCoroutine { continuation ->
            controller.setLedWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                endpoint = endpoint.toNSNumber(),
                isOn = isOn,
            ) { error ->
                continuation.handleResult(error)
            }
        }
    }

    override fun observeButtonChanges(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Boolean> {
        return callbackFlow {
            controller.observeButtonChangesWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                endpoint = endpoint.toNSNumber(),
                onUpdate = { trySend(it) }
            ) { error ->
                error?.let { close(IOSException(it)) }
            }

            awaitClose {  }
        }
    }
}
