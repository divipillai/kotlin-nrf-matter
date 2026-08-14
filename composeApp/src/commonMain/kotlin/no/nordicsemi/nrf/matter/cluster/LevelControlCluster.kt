package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

object LevelControlClusterInfo {
    const val ID: Long = 0x0008

    object Attribute {
        const val CURRENT_LEVEL: Long = 0x0000
    }
}

class LevelControlCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = LevelControlClusterInfo.ID

    /**
     * Sets the raw device level.
     *
     * TODO: use the MoveToLevelWithOnOff command once [MatterClient] can invoke commands with more
     *  than a single field; the command carries level, transition time and two option fields.
     */
    suspend fun setLevel(level: Int) {
        write(level.toUByte(), LevelControlClusterInfo.Attribute.CURRENT_LEVEL)
    }

    /** Emits the raw device level, reported as [Int] on Android and as [Long] on iOS. */
    suspend fun observeLevel(): Flow<Number> = observe(LevelControlClusterInfo.Attribute.CURRENT_LEVEL)
}
