@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.controller.MatterLightController
import no.nordicsemi.nrf.matter.model.DeviceId
import iosMatter.LocalMatterLightController

class MatterLightControllerImpl : MatterLightController {

    private val controller = LocalMatterLightController()

    override suspend fun setBrightnessLevel(
        deviceId: DeviceId,
        brightnessLevel: Int,
        endpoint: Int
    ) {
        return suspendCancellableCoroutine { continuation ->
            controller.setBrightnessLevelWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                endpoint = endpoint.toNSNumber(),
                brightnessLevel = brightnessLevel.toNSNumber()
            ) { error ->
                continuation.handleResult(error)
            }
        }
    }

    override suspend fun setDeviceOnOff(
        deviceId: DeviceId,
        isOn: Boolean,
        endpoint: Int
    ) {
        return suspendCancellableCoroutine { continuation ->
            controller.setDeviceOnOffWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                endpoint = endpoint.toNSNumber(),
                isOn = isOn
            ) { error ->
                continuation.handleResult(error)
            }
        }
    }

    override suspend fun observeLightState(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Boolean> {
        return callbackFlow {
            controller.observeLightStateWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                endpoint = endpoint.toNSNumber(),
                onUpdate = { trySend(it) }
            ) { error ->
                error?.let { close(IOSException(it)) }
            }

            awaitClose {  }
        }
    }

    override suspend fun observeBrightnessState(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Float> {
        return callbackFlow {
            controller.observeBrightnessStateWithDeviceId(
                deviceId = deviceId.toNSNumber(),
                endpoint = endpoint.toNSNumber(),
                onUpdate = { trySend(it) }
            ) { error ->
                error?.let { close(IOSException(it)) }
            }

            awaitClose {  }
        }
    }
}
