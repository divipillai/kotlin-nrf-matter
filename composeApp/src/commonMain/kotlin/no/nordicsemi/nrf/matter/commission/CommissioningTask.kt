package no.nordicsemi.nrf.matter.commission

import androidx.compose.runtime.Composable
import no.nordicsemi.nrf.matter.model.Device

@Composable
expect fun CommissioningTask(onSuccess: (Device) -> Unit, onError: (CommissioningException) -> Unit)
