package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.model.Device
import platform.Matter.MTRDeviceController
import platform.Matter.MTRKeypairProtocol

interface SwiftCodeProvider {

    fun getMatterControllerProvider(): MatterControllerProvider

    fun getMatterSupport(): MatterSupportKt

    fun getKeypair(): MTRKeypairProtocol

    fun getMatterOnOffController(): MatterOnOffController

    fun getMatterClusterDiscovery(): MatterClusterDiscovery
}

interface MatterSupportKt {

    suspend fun startIosCommissioning(onError: () -> Unit): Device?
}

interface MatterControllerProvider {

    fun getController(): MTRDeviceController?

    fun release()
}

interface MatterOnOffController {
    suspend fun turnOn()
    suspend fun turnOff()
}

interface MatterClusterDiscovery {
    suspend fun discoverClusters(nodeId: Int)
}
