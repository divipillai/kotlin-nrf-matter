@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.model.DeviceId
import platform.Foundation.NSNumber
import platform.Foundation.NSError
import swiftPMImport.no.nordicsemi.nrf.matter.shared.composeApp.LocalMatterCommissioner
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import swiftPMImport.no.nordicsemi.nrf.matter.shared.composeApp.Device as SwiftDevice

/**
 * Suspending wrapper over [LocalMatterCommissioner] from the `ios-matter` Swift package.
 *
 * The Swift `async throws` method is bridged into Objective-C as a completion-handler based
 * call, so it has to be adapted back into a suspend function here.
 */
internal suspend fun LocalMatterCommissioner.startIosCommissioning(
    deviceId: DeviceId,
): SwiftDevice = suspendCancellableCoroutine { continuation ->
    startIosCommissioningWithDeviceId(
        deviceId = NSNumber(long = deviceId.longValue),
    ) { device: SwiftDevice?, error: NSError? ->
        when {
            device != null -> continuation.resume(device)
            else -> continuation.resumeWithException(CommissioningException(error))
        }
    }
}

/** Failure reported by the `ios-matter` commissioning flow. */
internal class CommissioningException(
    private val error: NSError?,
) : Exception(error?.localizedDescription ?: "Commissioning failed.")
