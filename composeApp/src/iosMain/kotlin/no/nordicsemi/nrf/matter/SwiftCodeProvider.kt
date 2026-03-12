package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.model.Device

interface SwiftCodeProvider {

    fun getMatterCommissioner(): MatterCommissioner

    fun getMatterOnOffController(): MatterOnOffController

    fun getDecommissioner(): MatterDecommissioner
}

interface MatterCommissioner {

    suspend fun startIosCommissioning(onError: () -> Unit): Device?
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
