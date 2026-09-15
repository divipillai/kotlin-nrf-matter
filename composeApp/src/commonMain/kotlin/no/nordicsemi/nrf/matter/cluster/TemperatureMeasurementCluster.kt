package no.nordicsemi.nrf.matter.cluster

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.model.DeviceId

object TemperatureMeasurementClusterInfo {
    const val ID: Long = 0x0402

    object Attribute {
        const val MEASURED_VALUE: Long = 0x0000
    }
}

class TemperatureMeasurementCluster(
    override val deviceId: DeviceId,
    override val endpoint: Int,
    controller: MatterClient,
) : Cluster(controller) {

    override val id: Long = TemperatureMeasurementClusterInfo.ID

    fun observeMeasuredValue(): Flow<Number> = observeAttribute(TemperatureMeasurementClusterInfo.Attribute.MEASURED_VALUE)
}
