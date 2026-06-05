package no.nordicsemi.nrf.matter.commission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import no.nordicsemi.nrf.matter.HomeViewModel
import no.nordicsemi.nrf.matter.logger.NordicLogger
import org.koin.compose.viewmodel.koinViewModel

sealed interface CommissioningScreenState {
    data object InProgress : CommissioningScreenState
    data object Error : CommissioningScreenState
}

@Composable
fun CommissioningScreen(onBack: () -> Unit) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val state = remember { mutableStateOf<CommissioningScreenState>(CommissioningScreenState.InProgress) }

    CommissioningTask(
        onSuccess = {
            homeViewModel.addCommissionedDevice(device = it, true, false)
            onBack()
        },
        onError = {
            homeViewModel.commissioningFailed(1) //TODO result code
            state.value = CommissioningScreenState.Error
        },
    )

    NordicLogger.info("State: ${state.value}")

    when (state.value) {
        CommissioningScreenState.InProgress -> CommissioningInProgressScreen()
        CommissioningScreenState.Error -> CommissioningErrorScreen(onBack)
    }
}
