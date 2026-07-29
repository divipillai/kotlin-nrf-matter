package no.nordicsemi.nrf.matter.adapters

import kotlinx.coroutines.CancellableContinuation
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun <T> CancellableContinuation<T>.handleResult(error: NSError?, result: T? = null) {
    if (error != null) {
        resumeWithException(IOSException(error))
    } else if (result != null) {
        resume(result)
    } else {
        // Can occur for successful operations without a result.
    }
}
