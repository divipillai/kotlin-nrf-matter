@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import iosMatter.MatterCommissioner
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.logger.NordicLogger
import platform.Foundation.NSNumber

class AppExtensionMatterCommissioner {

    private val commissioner = MatterCommissioner()

    fun rooms(): List<String> {
        return listOf("Living Room", "Bedroom", "Office", "Kitchen", "Dining Room")
    }

    suspend fun commissionDevice(payload: String) {
        NordicLogger.info("Commission Matter device with payload: $payload")
        return suspendCancellableCoroutine { continuation ->
            commissioner.commissionWithPayload(payload, NSNumber(1)) { error ->
                continuation.handleResult(error)
            }
        }
    }

    fun releaseCommissioner() {
        NordicLogger.info("Releasing commissioner...")
        commissioner.releaseCommissioner()
    }

    fun onThreadNetworksDetected(names: List<String>) {
        NordicLogger.info("Selecting Thread network from ${names.size} scan results")

        names.forEach {
            NordicLogger.info("Detected thread network: $it.")
        }
    }
}
