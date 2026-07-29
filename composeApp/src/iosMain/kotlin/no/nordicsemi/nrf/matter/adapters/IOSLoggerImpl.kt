@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import no.nordicsemi.nrf.matter.logger.IOSLogger
import no.nordicsemi.nrf.matter.logger.LogEntity
import no.nordicsemi.nrf.matter.logger.LogLevel
import swiftPMImport.no.nordicsemi.nrf.matter.composeApp.IOSLoggerSwift

class IOSLoggerImpl : IOSLogger {

    private val swiftLogger = IOSLoggerSwift()

    override val logsChannel: Channel<String> = Channel(Channel.RENDEZVOUS)

    override fun getLogs(onReady: (List<LogEntity>) -> Unit) {
        swiftLogger.getLogsOnReady {
            val logs = it
                ?.filterIsInstance<swiftPMImport.no.nordicsemi.nrf.matter.composeApp.LogEntity>()
                ?.map { it.toDomain() }
            logs?.let { onReady(it) }
        }
    }

    override fun info(tag: String, message: String) {
        swiftLogger.infoWithTag(tag, message)
    }

    override fun debug(tag: String, message: String) {
        swiftLogger.debugWithTag(tag, message)
    }

    override fun error(tag: String, message: String) {
        swiftLogger.errorWithTag(tag, message)
    }
}

private fun swiftPMImport.no.nordicsemi.nrf.matter.composeApp.LogEntity.toDomain(): LogEntity {
    return LogEntity(
        date = this.date,
        level = this.level.toDomain(),
        tag = this.tag,
        message = this.message,
    )
}

private fun swiftPMImport.no.nordicsemi.nrf.matter.composeApp.LogLevel.toDomain(): LogLevel {
    return when (this) {
        0L -> LogLevel.INFO
        1L -> LogLevel.DEBUG
        else -> LogLevel.ERROR
    }
}
