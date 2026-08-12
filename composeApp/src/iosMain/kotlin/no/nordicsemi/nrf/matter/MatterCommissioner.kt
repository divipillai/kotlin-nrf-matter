package no.nordicsemi.nrf.matter

import no.nordicsemi.nrf.matter.domain.OperationResult
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId

interface MatterCommissioner {

    suspend fun startIosCommissioning(deviceId: DeviceId): OperationResult<Device>
}
