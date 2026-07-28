@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import no.nordicsemi.nrf.matter.logger.IOSLogger
import no.nordicsemi.nrf.matter.logger.LogEntity
import swiftPMImport.no.nordicsemi.nrf.matter.shared.composeApp.IOSLoggerSwift

class IOSLoggerImpl : IOSLogger {

    private val swiftLogger = IOSLoggerSwift()

    override val logsChannel: Channel<String> = Channel(Channel.RENDEZVOUS)

    override fun getLogs(onReady: (List<LogEntity>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun info(tag: String, message: String) {
//        swiftLogger.
    }

    override fun debug(tag: String, message: String) {
        TODO("Not yet implemented")
    }

    override fun error(tag: String, message: String) {
        TODO("Not yet implemented")
    }


}