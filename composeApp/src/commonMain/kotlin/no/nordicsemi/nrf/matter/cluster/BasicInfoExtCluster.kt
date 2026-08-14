package no.nordicsemi.nrf.matter.cluster

import no.nordicsemi.nrf.matter.model.DeviceId

object BasicInfoClusterInfo {
    const val ID: Long = 0x28

    object Attribute {
        const val RANDOM_NUMBER: Long = 0x17
    }

    object Command {
        const val GENERATE_RANDOM_NUMBER: Long = 0x00
    }
}

class BasicInfoExtCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = BasicInfoClusterInfo.ID

    /** Asks the device for a new random number and reads the generated value back. */
    suspend fun generateRandomNumber(): Long {
        execute(commandId = BasicInfoClusterInfo.Command.GENERATE_RANDOM_NUMBER)
        return read<Number>(BasicInfoClusterInfo.Attribute.RANDOM_NUMBER).toLong()
    }
}
