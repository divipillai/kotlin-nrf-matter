package no.nordicsemi.nrf.matter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform