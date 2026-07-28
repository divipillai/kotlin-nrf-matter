package no.nordicsemi.nrf.matter.adapters

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.controller.MatterLightController
import no.nordicsemi.nrf.matter.model.DeviceId

class MatterLightControllerImpl : MatterLightController {
    override suspend fun setBrightnessLevel(
        deviceId: DeviceId,
        brightnessLevel: Int,
        endpoint: Int
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun setDeviceOnOff(
        deviceId: DeviceId,
        isOn: Boolean,
        endpoint: Int
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun observeLightState(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun observeBrightnessState(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Float> {
        TODO("Not yet implemented")
    }
}