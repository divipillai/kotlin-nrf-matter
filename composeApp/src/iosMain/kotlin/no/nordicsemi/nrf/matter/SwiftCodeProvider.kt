package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.model.Device
import platform.Foundation.NSData

interface SwiftCodeProvider {

    fun getThreadNetworkProvider(): ThreadNetworkProvider

    fun getMatterSupport(): MatterSupportKt
}

interface ThreadNetworkProvider {

    suspend fun getAvailableThreadNetworks(): List<ThreadNetwork>
}

interface MatterSupportKt {

    suspend fun startIosCommissioning(code: String, onError: () -> Unit): Device?
}

data class ThreadNetwork(
    val name: String,
    val data: NSData?,
)
