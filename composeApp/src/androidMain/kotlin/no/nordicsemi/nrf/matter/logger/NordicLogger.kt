package no.nordicsemi.nrf.matter.logger

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

actual object NordicLogger {

    private val logs = MutableStateFlow<List<LogEntity>>(emptyList()) //TODO: make persistent

    actual fun getLogs(): Flow<List<LogEntity>> {
        return logs
    }

    actual fun info(tag: String, message: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.INFO,
            tag = tag,
            message = message,
        )
        logs.update { it + logEntity }
        Napier.i(tag = tag, message = message)
    }

    actual fun debug(tag: String, message: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.DEBUG,
            tag = tag,
            message = message,
        )
        logs.update { it + logEntity }
        Napier.d(tag = tag, message = message)
    }

    actual fun error(tag: String, message: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.ERROR,
            tag = tag,
            message = message,
        )
        logs.update { it + logEntity }
        Napier.e(tag = tag, message = message)
    }
}
