package no.nordicsemi.nrf.matter.logger

enum class LogLevel {
    INFO,
    DEBUG,
    ERROR
}

class LogEntity (
    val date: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
)
