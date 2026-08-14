package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

class LevelControlCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = ID

    /**
     * Sets the raw device level (see [MIN_LEVEL]..[MAX_LEVEL]).
     *
     * TODO: use the MoveToLevelWithOnOff command once [MatterClient] can invoke commands with more
     *  than a single field; the command carries level, transition time and two option fields.
     */
    suspend fun setLevel(level: Int) {
        write(level.toUByte(), CURRENT_LEVEL_ATTRIBUTE_ID)
    }

    /** Emits the raw device level, reported as [Int] on Android and as [Long] on iOS. */
    suspend fun observeLevel(): Flow<Number> = observe(CURRENT_LEVEL_ATTRIBUTE_ID)

    companion object {
        const val ID: Long = 0x0008

        const val MIN_LEVEL: Int = 1
        const val MAX_LEVEL: Int = 254

        private const val CURRENT_LEVEL_ATTRIBUTE_ID: Long = 0x0000
    }
}
