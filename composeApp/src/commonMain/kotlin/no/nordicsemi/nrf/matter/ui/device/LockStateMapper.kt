package no.nordicsemi.nrf.matter.ui.device

import no.nordicsemi.nrf.matter.domain.UiState
import no.nordicsemi.nrf.matter.model.LockDeviceState

/** Maps the raw LockState attribute value, or `null` when the device reported an unknown state. */
fun Number.toLockDeviceState(): LockDeviceState? =
    LockDeviceState.entries.firstOrNull { it.value == toInt() }

/** A lock which is between two states is presented as still working. */
fun LockDeviceState.toUiState(): UiState<LockDeviceState> = when (this) {
    LockDeviceState.LOCKED,
    LockDeviceState.UNLOCKED -> UiState.Success(this)

    LockDeviceState.NOT_FULLY_LOCKED,
    LockDeviceState.UNLATCHED -> UiState.Loading()
}
