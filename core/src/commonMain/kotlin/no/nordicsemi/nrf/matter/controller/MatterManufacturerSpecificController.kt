package no.nordicsemi.nrf.matter.controller

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

interface MatterManufacturerSpecificController {

    /**
     * Sends the vendor-specific "set LED" command.
     *
     * The command always targets [LED_ENDPOINT] using the fixed [MANUFACTURER_SPECIFIC_CLUSTER_ID] /
     * [SET_LED_COMMAND_ID] pair; the endpoint, cluster ID and command ID are not resolved from the
     * device's descriptor cluster, so this only works against a device that exposes this exact
     * cluster/command combination on that endpoint.
     *
     * @param deviceId the commissioned device to control.
     * @throws IllegalStateException if the device pointer cannot be resolved (e.g. device
     * unreachable).
     */
    suspend fun setLed(deviceId: DeviceId)

    /**
     * Sends a vendor-specific "generate random number" invoke, then reads back the resulting
     * value.
     *
     * Targets cluster [RANDOM_NUMBER_CLUSTER_ID] (Basic Information) on endpoint
     * [RANDOM_NUMBER_ENDPOINT], invoking [RANDOM_NUMBER_COMMAND_ID] and then reading
     * [RANDOM_NUMBER_ATTRIBUTE_ID] for the result. These IDs are fixed rather than resolved from
     * the device's descriptor cluster, and depend on the companion firmware exposing this vendor
     * command on that cluster.
     *
     * @param deviceId the commissioned device to invoke.
     * @return the generated random number, or `null` if the device is unreachable, the command is
     * rejected, or the attribute can't be read back.
     */
    suspend fun generateRandomNumber(deviceId: DeviceId): Long?

    /**
     * Subscribes to the vendor-specific button-pressed attribute and emits its state as it
     * changes.
     *
     * The subscription reports changes instantly and otherwise sends a heartbeat every 30 seconds;
     * establishing the underlying session is subject to a 30-second timeout. The returned [Flow]
     * closes with an exception if the subscription cannot be established.
     *
     * @param deviceId the commissioned device to observe.
     * @param endpoint the Matter endpoint exposing the manufacturer-specific cluster.
     * @return a cold [Flow] emitting `true` when the button is pressed, `false` when released.
     */
    fun observeButtonChanges(deviceId: DeviceId, endpoint: Int): Flow<Boolean>
}