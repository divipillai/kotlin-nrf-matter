package no.nordicsemi.nrf.matter.controller

import no.nordicsemi.nrf.matter.model.DeviceId

interface MatterClusterExtensionController {

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
    suspend fun generateRandomNumber(deviceId: DeviceId, endpoint: Int): Long?
}
