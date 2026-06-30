package no.nordicsemi.nrf.matter.logger

import kotlinx.coroutines.channels.Channel

abstract class IOSLogger {

    val logsChannel: Channel<String> = Channel(Channel.BUFFERED)

    abstract fun getLogs(onReady: (List<LogEntity>) -> Unit)

    abstract fun info(tag: String, message: String)

    abstract fun debug(tag: String, message: String)

    abstract fun error(tag: String, message: String)
}
