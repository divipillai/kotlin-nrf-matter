package no.nordicsemi.nrf.matter.commission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import no.nordicsemi.nrf.matter.domain.OperationResult
import no.nordicsemi.nrf.matter.model.Device
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun CommissioningTask(onSuccess: (Device) -> Unit, onError: (CommissioningException) -> Unit) {
    val commissioningViewModel: CommissioningViewModelIos = koinViewModel()

    LaunchedEffect(Unit) {
        when (val result = commissioningViewModel.startIosCommissioning()) {
            is OperationResult.Success -> onSuccess(result.data)
            is OperationResult.Error -> (result.t as? CommissioningException)?.let {
                onError(it)
            }
        }
    }
}
