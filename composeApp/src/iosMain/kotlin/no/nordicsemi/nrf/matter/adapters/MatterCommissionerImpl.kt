@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import iosMatter.LocalMatterCommissioner
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.MatterCommissioner
import no.nordicsemi.nrf.matter.commission.toCommissioningException
import no.nordicsemi.nrf.matter.domain.OperationResult
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import kotlin.coroutines.resume

class MatterCommissionerImpl : MatterCommissioner {

    private val localMatterCommissioner = LocalMatterCommissioner()

    override suspend fun startIosCommissioning(deviceId: DeviceId): OperationResult<Device> {
        return suspendCancellableCoroutine { continuation ->
            localMatterCommissioner.startIosCommissioningWithDeviceId(deviceId.toNSNumber()) { device, error ->
                val result = error?.toCommissioningException()
                    ?.let { OperationResult.Error(it) }
                    ?: device?.let { OperationResult.Success(it.toDomain()) }
                    ?: throw IllegalArgumentException("Both error and success are null.")

                continuation.resume(result)
            }
        }
    }
}
