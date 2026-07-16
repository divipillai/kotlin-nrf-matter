package no.nordicsemi.nrf.matter.chip

import chip.devicecontroller.model.ChipAttributePath
import no.nordicsemi.nrf.matter.controller.MatterClusterExtensionController
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.DeviceId

class MatterClusterExtensionControllerImpl(
    private val chipClient: ChipClient,
) : MatterClusterExtensionController {

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
     * @param endpoint the Matter endpoint exposing the vendor-specific cluster.
     * @return the generated random number, or `null` if the device is unreachable, the command is
     * rejected, or the attribute can't be read back.
     */
    override suspend fun generateRandomNumber(deviceId: DeviceId, endpoint: Int): Long? {
        return try {
            val connectedDevicePtr = connectedDevicePointer(deviceId)
            chipClient.generateRandomNumber(
                connectedDevicePtr,
                ChipAttributePath.newInstance(
                    endpoint,
                    RANDOM_NUMBER_CLUSTER_ID,
                    RANDOM_NUMBER_COMMAND_ID,
                )
            )
            val namePath = ChipAttributePath.newInstance(
                endpoint,
                RANDOM_NUMBER_CLUSTER_ID,
                RANDOM_NUMBER_ATTRIBUTE_ID,
            )
            val nameAttr = chipClient.readAttribute(connectedDevicePtr, namePath)
            nameAttr?.value as? Long
        } catch (t: Throwable) {
            NordicLogger.error("Random number generation failed: ${t.message}", t, tag = TAG)
            t.printStackTrace()
            null
        }
    }

    private suspend fun connectedDevicePointer(deviceId: DeviceId): Long =
        chipClient.getConnectedDevicePointer(deviceId.longValue)

    companion object {
        private const val RANDOM_NUMBER_CLUSTER_ID = 0x28L
        private const val RANDOM_NUMBER_COMMAND_ID = 0x00L
        private const val RANDOM_NUMBER_ATTRIBUTE_ID = 0x00017L

        private val TAG: String
            get() = "MatterClusterExtensionControllerImpl"
    }
}