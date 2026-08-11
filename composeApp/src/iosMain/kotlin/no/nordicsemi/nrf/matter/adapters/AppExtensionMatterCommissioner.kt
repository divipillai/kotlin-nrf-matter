@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import iosMatter.MatterCommissioner
import iosMatter.SharedConsts
import iosMatter.SharedStorage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.commission.CommissioningException
import no.nordicsemi.nrf.matter.logger.NordicLogger

/**
 * Kotlin side of the Matter "Add Device" app extension.
 *
 * The extension runs in its own process, so it has to set up the Kotlin runtime on its own; nothing
 * the app does on start-up applies here.
 */
class AppExtensionMatterCommissioner {

    private val TAG = "AppExtension"

    private val commissioner = MatterCommissioner()
    private val storage = SharedStorage()

    init {
        NordicLogger.setLogger(IOSLoggerImpl())
    }

    fun rooms(): List<String> {
        NordicLogger.info("Getting rooms.", tag = TAG)
        return listOf("Living Room", "Bedroom", "Office", "Kitchen", "Dining Room")
    }

    /**
     * Commissions the device described by [payload] onto the local fabric.
     *
     * Declared with [Throws] so that a failure is handed to the extension as an `NSError` instead
     * of an unhandled Kotlin exception, which would take the extension process down and leave the
     * system add-device flow waiting forever.
     */
    @Throws(
        CommissioningException::class,
        IOSException::class,
        IllegalStateException::class,
    )
    suspend fun commissionDevice(payload: String) {
        NordicLogger.info("Commission Matter device with payload: $payload", tag = TAG)
        val nodeId = storage.getNumberWithKey(SharedConsts.nodeIdKey)
            ?: error("No node ID found in shared storage.")
        return suspendCancellableCoroutine { continuation ->
            commissioner.commissionWithPayload(payload, nodeId) { error ->
                continuation.handleResult(error)
            }
        }
    }

    /**
     * Records the successful outcome for the app to pick up once the extension closes, and releases
     * the Matter controller.
     */
    fun configureDevice() {
        NordicLogger.info(
            "Device configured. Storing result and releasing commissioner...",
            tag = TAG,
        )
        storage.storeBoolWithKey(SharedConsts.resultKey, true)
        commissioner.releaseCommissioner()
    }

    fun onThreadNetworksDetected(names: List<String>) {
        NordicLogger.info(
            "Selecting Thread network from ${names.size} scan results",
            tag = TAG,
        )

        names.forEach {
            NordicLogger.info("Detected thread network: $it.", tag = TAG)
        }
    }
}
