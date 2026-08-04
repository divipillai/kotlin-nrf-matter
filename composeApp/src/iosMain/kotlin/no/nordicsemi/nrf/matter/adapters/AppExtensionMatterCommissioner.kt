@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import iosMatter.MatterCommissioner
import iosMatter.SharedConsts
import iosMatter.SharedStorage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.logger.NordicLogger

class AppExtensionMatterCommissioner {

    private val commissioner = MatterCommissioner()

    fun rooms(): List<String> {
        return listOf("Living Room", "Bedroom", "Office", "Kitchen", "Dining Room")
    }

    suspend fun commissionDevice(payload: String) {
        NordicLogger.info("Commission Matter device with payload: $payload")
        val sharedStorage = SharedStorage()
        val nodeId = sharedStorage.getNumberWithKey(SharedConsts.nodeIdKey)!!
        return suspendCancellableCoroutine { continuation ->
            commissioner.commissionWithPayload(payload, nodeId) { error ->
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
