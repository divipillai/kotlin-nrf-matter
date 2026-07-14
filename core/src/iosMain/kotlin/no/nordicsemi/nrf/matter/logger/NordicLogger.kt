package no.nordicsemi.nrf.matter.logger

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual object NordicLogger {

    private lateinit var logger: IOSLogger

    val logsChannel
        get() = logger.logsChannel

    fun setLogger(logger: IOSLogger) {
        this.logger = logger
    }

    actual fun getLogs(): Flow<List<LogEntity>> {
        return callbackFlow {
            logger.getLogs { trySend(it) }

            awaitClose {
                
            }
        }
    }

    actual fun info(message: String, tag: String) {
        logger.info(tag, message)
    }

    actual fun debug(message: String, tag: String) {
        logger.debug(tag, message)
    }

    actual fun error(message: String, t: Throwable?, tag: String) {
        val fullMessage = buildString {
            append(message)
            if (t != null) {
                appendLine()
                append(t.stackTraceToString())
            }
        }
        logger.error(tag, fullMessage)
    }
}
