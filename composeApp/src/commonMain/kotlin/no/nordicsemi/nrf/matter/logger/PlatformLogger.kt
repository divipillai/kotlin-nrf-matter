package no.nordicsemi.nrf.matter.logger

import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PlatformLogger(private val logger: NativePlatformLogger) {

    fun getLogs(): Flow<List<LogEntity>> {
        Napier.i { "AAATESTAAA - getLogs" }
        return callbackFlow {
            logger.getLogs {
                Napier.i { "AAATESTAAA - items received: ${it.size}" }
                trySend(it)
            }

            awaitClose {

            }
        }
    }

    fun info(tag: String, message: String) {
        logger.info(tag, message)
    }

    fun debug(tag: String, message: String) {
        logger.debug(tag, message)
    }

    fun error(tag: String, message: String) {
        logger.error(tag, message)
    }
}
