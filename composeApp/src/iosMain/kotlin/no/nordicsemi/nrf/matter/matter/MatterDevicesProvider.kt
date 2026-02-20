package no.nordicsemi.nrf.matter.matter

import platform.Matter.MTRDevice

object MatterDevicesProvider {

    private var device: MTRDevice? = null

    fun getDevice(): MTRDevice? {
        return device
    }

    fun saveDevice(device: MTRDevice) {
        this.device = device
    }
}
