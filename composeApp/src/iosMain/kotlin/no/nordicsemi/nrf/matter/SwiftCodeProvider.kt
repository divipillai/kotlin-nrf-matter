package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId

interface SwiftCodeProvider {

    fun getMatterCommissioner(): MatterCommissioner

    fun getMatterOnOffController(): MatterOnOffController

    fun getDecommissioner(): MatterDecommissioner
}

interface MatterCommissioner {

    suspend fun startIosCommissioning(onError: () -> Unit): Device?
}

interface MatterDecommissioner {

    suspend fun decommission(deviceId: DeviceId)
}

interface MatterOnOffController {

    suspend fun setDeviceOnOff(
        deviceId: DeviceId,
        isDeviceOnline: Boolean,
        isOn: Boolean,
        endpoint: Int,
    )
}
