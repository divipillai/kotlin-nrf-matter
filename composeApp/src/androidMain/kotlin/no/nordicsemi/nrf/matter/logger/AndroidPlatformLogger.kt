package no.nordicsemi.nrf.matter.logger

import io.github.aakira.napier.Napier

class AndroidPlatformLogger : NativePlatformLogger {

    private val logs = mutableListOf<LogEntity>() //TODO: make persistent

    override fun getLogs(onReady: (List<LogEntity>) -> Unit) {
        onReady(logs.toList())
    }

    override fun info(tag: String, message: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.INFO,
            tag = tag,
            message = message,
        )
        logs.add(logEntity)
        Napier.i(tag = tag, message = message)
    }

    override fun debug(tag: String, message: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.DEBUG,
            tag = tag,
            message = message,
        )
        logs.add(logEntity)
        Napier.d(tag = tag, message = message)
    }

    override fun error(tag: String, message: String) {
        val logEntity = LogEntity(
            date = System.currentTimeMillis(),
            level = LogLevel.ERROR,
            tag = tag,
            message = message,
        )
        logs.add(logEntity)
        Napier.e(tag = tag, message = message)
    }
}
