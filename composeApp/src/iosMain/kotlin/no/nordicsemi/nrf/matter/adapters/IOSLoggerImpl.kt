@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.channels.Channel
import no.nordicsemi.nrf.matter.logger.IOSLogger
import no.nordicsemi.nrf.matter.logger.LogEntity
import no.nordicsemi.nrf.matter.logger.LogLevel
import platform.Foundation.NSError
import iosMatter.LogLevelDebug
import iosMatter.LogLevelInfo
import iosMatter.SwiftLogger
import iosMatter.LogEntity as SwiftLogEntity

class IOSLoggerImpl : IOSLogger {

    override val logsChannel: Channel<String> = Channel(Channel.RENDEZVOUS)

    init {
        SwiftLogger.callback = { entry ->
            entry?.let { logsChannel.trySend(it.message) }
        }
    }

    override fun getLogs(onReady: (List<LogEntity>) -> Unit) {
        val logs = memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            SwiftLogger.logsAndReturnError(error.ptr)
                ?.filterIsInstance<SwiftLogEntity>()
                ?.map { it.toDomain() }
        }
        onReady(logs ?: emptyList())
    }

    override fun info(tag: String, message: String) {
        SwiftLogger.infoWithTag(tag, message)
    }

    override fun debug(tag: String, message: String) {
        SwiftLogger.debugWithTag(tag, message)
    }

    override fun error(tag: String, message: String) {
        SwiftLogger.errorWithTag(tag, message)
    }
}

private fun SwiftLogEntity.toDomain(): LogEntity {
    return LogEntity(
        date = this.date,
        level = this.level.toDomain(),
        tag = this.tag,
        message = this.message,
    )
}

private fun Long.toDomain(): LogLevel {
    return when (this) {
        LogLevelInfo -> LogLevel.INFO
        LogLevelDebug -> LogLevel.DEBUG
        else -> LogLevel.ERROR
    }
}
