@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.MatterCommissioner
import no.nordicsemi.nrf.matter.domain.OperationResult
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import swiftPMImport.no.nordicsemi.nrf.matter.composeApp.LocalMatterCommissioner

class MatterCommissionerImpl : MatterCommissioner {

    private val localMatterCommissioner = LocalMatterCommissioner()

    override suspend fun startIosCommissioning(deviceId: DeviceId): OperationResult<Device> {
        return suspendCancellableCoroutine { continuation ->
            localMatterCommissioner.startIosCommissioningWithDeviceId(deviceId.toNSNumber()) { device, error ->
                continuation.handleResult(
                    error = error,
                    result = device?.let { OperationResult.Success(it.toDomain()) }
                )
            }
        }
    }
}