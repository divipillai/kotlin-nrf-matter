package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.model.Device
import platform.Matter.MTRDeviceController
import platform.Matter.MTRKeypairProtocol

interface SwiftCodeProvider {

    fun getMatterControllerProvider(): MatterControllerProvider

    fun getMatterSupport(): MatterSupportKt

    fun getKeypair(): MTRKeypairProtocol

    fun getMatterOnOffController(): MatterOnOffController

    fun getDecommissioner(): MatterDecommissioner
}

interface MatterSupportKt {

    suspend fun startIosCommissioning(onError: () -> Unit): Device?
}

interface MatterControllerProvider {

    fun getController(): MTRDeviceController?

    fun release()
}

interface MatterDecommissioner {

    fun decommission(nodeId: Long)
}

interface MatterOnOffController {

    suspend fun setDeviceOnOff(
        deviceId: Long,
        isDeviceOnline: Boolean,
        isOn: Boolean,
        endpoint: Int,
    )
}
