@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.controller.MatterClusterExtensionController
import no.nordicsemi.nrf.matter.model.DeviceId
import swiftPMImport.no.nordicsemi.nrf.matter.composeApp.LocalMatterClusterExtController

class MatterClusterExtensionControllerImpl : MatterClusterExtensionController {

    private val controller = LocalMatterClusterExtController()

    override suspend fun generateRandomNumber(
        deviceId: DeviceId,
        endpoint: Int
    ): Long? {
        return suspendCancellableCoroutine { continuation ->
            controller.generateRandomNumberWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                endpoint.toNSNumber()
            ) { result, error ->
                continuation.handleResult(error, result.toLong())
            }
        }
    }
}