package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.model.Device

interface SwiftCodeProvider {

    fun getThreadNetworkProvider(): ThreadNetworkProvider

    fun getMatterSupport(): MatterSupportKt
}

interface ThreadNetworkProvider {

    fun getAvailableThreadNetworks(): List<ThreadNetwork>
}

interface MatterSupportKt {

    suspend fun startIosCommissioning(code: String, onError: () -> Unit): Device?
}

data class ThreadNetwork(
    val name: String
)