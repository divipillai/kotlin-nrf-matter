package no.nordicsemi.nrf.matter.commission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun CommissioningTask(onSuccess: (Device) -> Unit, onError: (CommissioningException) -> Unit) {
    val commissioningViewModel: CommissioningViewModelIos = koinViewModel()

    LaunchedEffect(Unit) {
        try {
            val device = commissioningViewModel.startIosCommissioning()
            onSuccess(device)
        } catch (e: CommissioningException) {
            NordicLogger.error("Unable to commission device", e)
            onError(e)
        }
    }
}
