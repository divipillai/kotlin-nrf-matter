package no.nordicsemi.nrf.matter.logger

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

actual object NordicLogger {

    private val logs = MutableStateFlow<List<LogEntity>>(emptyList()) //TODO: make persistent

    actual fun getLogs(): Flow<List<LogEntity>> {
        return logs
    }

    actual fun info(message: String, tag: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.INFO,
            tag = tag,
            message = message,
        )
        logs.update { it + logEntity }
        Log.i(tag, message)
    }

    actual fun debug(message: String, tag: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.DEBUG,
            tag = tag,
            message = message,
        )
        logs.update { it + logEntity }
        Log.d(tag, message)
    }

    actual fun error(message: String, t: Throwable?, tag: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.ERROR,
            tag = tag,
            message = message,
        )
        logs.update { it + logEntity }
        Log.e(tag, message, t)
    }
}
