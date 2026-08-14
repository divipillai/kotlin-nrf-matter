package no.nordicsemi.nrf.matter.cluster

import no.nordicsemi.nrf.matter.model.DeviceId


class BasicInfoExtCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = ID

    /** Asks the device for a new random number and reads the generated value back. */
    suspend fun generateRandomNumber(): Long {
        execute(commandId = GENERATE_RANDOM_NUMBER_COMMAND_ID)
        return read<Number>(RANDOM_NUMBER_ATTRIBUTE_ID).toLong()
    }

    companion object {
        const val ID: Long = 0x28

        private const val RANDOM_NUMBER_ATTRIBUTE_ID: Long = 0x17
        private const val GENERATE_RANDOM_NUMBER_COMMAND_ID: Long = 0x00
    }
}
