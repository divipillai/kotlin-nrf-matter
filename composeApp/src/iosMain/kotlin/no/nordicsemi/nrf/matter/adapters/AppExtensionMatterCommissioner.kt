@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import iosMatter.MatterCommissioner
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSNumber

class AppExtensionMatterCommissioner {

    private val commissioner = MatterCommissioner()

    suspend fun commission(payload: String, deviceId: NSNumber) {
        return suspendCancellableCoroutine { continuation ->
            commissioner.commissionWithPayload(payload, deviceId) { error ->
                continuation.handleResult(error)
            }
        }
    }

    fun releaseCommissioner() {
        commissioner.releaseCommissioner()
    }
}
