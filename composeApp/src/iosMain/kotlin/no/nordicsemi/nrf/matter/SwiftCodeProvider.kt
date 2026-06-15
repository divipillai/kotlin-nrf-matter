package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.domain.ManufacturerSpecificData
import no.nordicsemi.nrf.matter.logger.IOSLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId

interface SwiftCodeProvider {

    fun getMatterCommissioner(): MatterCommissioner

    fun getMatterOnOffController(): MatterLightController

    fun getDecommissioner(): MatterDecommissioner

    fun getMatterBinder(): MatterBinder

    fun getMatterDoorController(): MatterDoorController

    fun getMatterManufacturerCustomDataController(): MatterManufacturerCustomDataController

    fun getMatterClusterExtensionController(): MatterClusterExtensionController

    fun getLogger(): IOSLogger
}

interface MatterCommissioner {

    suspend fun startIosCommissioning(deviceId: DeviceId): Device
}

interface MatterDecommissioner {

    suspend fun decommission(deviceId: DeviceId)
}

interface MatterLightController {

    suspend fun setDeviceOnOff(
        deviceId: DeviceId,
        isOn: Boolean,
        endpoint: Int,
    )

    suspend fun setBrightnessLevel(
        deviceId: DeviceId,
        level: Int,
        endpoint: Int,
    )
}

interface MatterBinder {

    suspend fun bind(
        sourceNodeId: DeviceId,
        sourceEndpoint: Int,
        targetNodeId: DeviceId,
        targetEndpoint: Int,
        clusterId: Long
    )
}

interface MatterDoorController {

    suspend fun lockUnlockDoor(
        deviceId: DeviceId,
        isLocked: Boolean,
        endpoint: Int
    )
}

interface MatterManufacturerCustomDataController {

    suspend fun setLed(
        deviceId: DeviceId,
        isOn: Boolean,
        endpoint: Int,
    )

    suspend fun getData(deviceId: DeviceId, endpoint: Int): ManufacturerSpecificData

    suspend fun subscribeToButtonChanges(
        deviceId: DeviceId,
        endpoint: Int,
        onUpdate: (Boolean) -> Unit
    )
}

interface MatterClusterExtensionController {

    suspend fun generateRandomNumber(deviceId: DeviceId, endpoint: Int): Int
}
