package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

class ManufacturerSpecCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = ID

    suspend fun setLed(isOn: Boolean) {
        execute(commandId = SET_LED_COMMAND_ID, value = if (isOn) ON_VALUE else OFF_VALUE)
    }

    suspend fun observeLed(): Flow<Boolean> = observe(LED_ATTRIBUTE_ID)

    suspend fun observeButton(): Flow<Boolean> = observe(BUTTON_ATTRIBUTE_ID)

    suspend fun readName(): String = read(NAME_ATTRIBUTE_ID)

    companion object {
        const val ID: Long = 0xFFF1FC01

        private const val NAME_ATTRIBUTE_ID: Long = 0xFFF10000
        private const val LED_ATTRIBUTE_ID: Long = 0xFFF10001
        private const val BUTTON_ATTRIBUTE_ID: Long = 0xFFF10002

        private const val SET_LED_COMMAND_ID: Long = 0xFFF10000

        // The command carries the new LED state as a single uint8 field.
        private val ON_VALUE: UByte = 1u
        private val OFF_VALUE: UByte = 0u
    }
}
