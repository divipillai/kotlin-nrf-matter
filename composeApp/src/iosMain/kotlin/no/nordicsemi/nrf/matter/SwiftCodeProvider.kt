package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.model.Device
import platform.Foundation.NSData
import platform.Matter.MTRDeviceController
import platform.Matter.MTRKeypairProtocol

interface SwiftCodeProvider {

    fun getMatterControllerProvider(): MatterControllerProvider

    fun getThreadNetworkProvider(): ThreadNetworkProvider

    fun getMatterSupport(): MatterSupportKt

    fun getKeypair(): MTRKeypairProtocol

    fun getMatterOnOffController(): MatterOnOffController
}

interface ThreadNetworkProvider {

    suspend fun getAvailableThreadNetworks(): List<ThreadNetwork>
}

interface MatterSupportKt {

    suspend fun startIosCommissioning(code: String, onError: () -> Unit): Device?
}

interface MatterControllerProvider {

    fun getController(): MTRDeviceController?

    fun release()
}

interface MatterOnOffController {
    fun turnOn()
    fun turnOff()
}

data class ThreadNetwork(
    val name: String,
    val data: NSData?,
)
