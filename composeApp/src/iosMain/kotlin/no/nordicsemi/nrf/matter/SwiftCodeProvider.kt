package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.controller.BindingController
import no.nordicsemi.nrf.matter.controller.MatterClusterExtensionController
import no.nordicsemi.nrf.matter.controller.MatterDecommissioner
import no.nordicsemi.nrf.matter.controller.MatterDoorLockController
import no.nordicsemi.nrf.matter.controller.MatterLightController
import no.nordicsemi.nrf.matter.controller.MatterManufacturerSpecificController
import no.nordicsemi.nrf.matter.domain.OperationResult
import no.nordicsemi.nrf.matter.logger.IOSLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId

interface SwiftCodeProvider {

    fun getMatterCommissioner(): MatterCommissioner

    fun getMatterOnOffController(): MatterLightController

    fun getDecommissioner(): MatterDecommissioner

    fun getMatterBinder(): BindingController

    fun getMatterDoorController(): MatterDoorLockController

    fun getMatterManufacturerCustomDataController(): MatterManufacturerSpecificController

    fun getMatterClusterExtensionController(): MatterClusterExtensionController

    fun getLogger(): IOSLogger
}

interface MatterCommissioner {

    suspend fun startIosCommissioning(deviceId: DeviceId): OperationResult<Device>
}
