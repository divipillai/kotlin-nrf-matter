package no.nordicsemi.nrf.matter.logger

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

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
) {
    val formattedDate = lazy { //FIXME - date is wrong (1970)
        Instant.fromEpochMilliseconds(date)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
            .replace("T", " ")
    }
}
