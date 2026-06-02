package no.nordicsemi.nrf.matter.logger

import kotlinx.coroutines.flow.Flow

expect object NordicLogger {

    fun getLogs(): Flow<List<LogEntity>>

    fun info(tag: String, message: String)

    fun debug(tag: String, message: String)

    fun error(tag: String, message: String)
}
