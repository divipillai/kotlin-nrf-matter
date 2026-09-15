package no.nordicsemi.nrf.matter.ui.contact

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.nrf.matter.cluster.ContactSensorCluster
import no.nordicsemi.nrf.matter.ui.device.ClusterController

data class ContactSensorState(
    val isContactDetected: Boolean = false,
)

class ContactSensorController(
    cluster: ContactSensorCluster,
    scope: CoroutineScope,
) : ClusterController(scope) {

    private val _state = MutableStateFlow(ContactSensorState())
    val state = _state.asStateFlow()

    init {
        cluster.observeStateValue()
            .onEach { isContactDetected -> _state.update { it.copy(isContactDetected = isContactDetected) } }
            .launchIn(scope)
    }
}
