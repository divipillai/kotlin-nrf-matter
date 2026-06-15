package no.nordicsemi.nrf.matter.ui.lock

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

data class LockDeviceState(
    val isLocked: Boolean = false
)

class LockController(
    private val device: DeviceUiModel,
    private val commandHandler: LockCommandHandler,
    private val scope: CoroutineScope,
) : MatterController {

    val lockState = MutableStateFlow<UiState<LockDeviceState>>(UiState.Idle())

    init {
        observeDeviceRealtimeState()
    }

    private fun observeDeviceRealtimeState() {
        commandHandler.observeLockDeviceState(device.device)
            .onEach {
                lockState.value = it
            }
            .launchIn(scope)
    }

    fun setLock(device: Device, isOn: Boolean) {
        commandHandler.handleLock(device, isOn)
            .catch {
                NordicLogger.error(
                    "Failed to send Lock/Unlock command",
                    it,
                    tag = "LockController"
                )
            }
            .launchIn(scope)
    }

    @Composable
    override fun Item(onDecommission: (DeviceId) -> Unit) {
        LockItem(
            device = device,
            lockState = lockState.collectAsStateWithLifecycle().value,
            onLockUnlockDoor = { _, state ->
                setLock(device.device, state)
            },
            onDecommission = onDecommission,
        )
    }
}
