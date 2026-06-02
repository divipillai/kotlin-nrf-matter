package no.nordicsemi.nrf.matter.logger

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual object NordicLogger {

    private lateinit var logger: IOSLogger

    internal fun setLogger(logger: IOSLogger) {
        this.logger = logger
    }

    actual fun getLogs(): Flow<List<LogEntity>> {
        return callbackFlow {
            logger.getLogs { trySend(it) }
        }
    }

    actual fun info(tag: String, message: String) {
        logger.info(tag, message)
    }

    actual fun debug(tag: String, message: String) {
        logger.debug(tag, message)
    }

    actual fun error(tag: String, message: String) {
        logger.error(tag, message)
    }
}
