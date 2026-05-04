@file:OptIn(ExperimentalAtomicApi::class)

package no.nordicsemi.nrf.matter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import no.nordicsemi.nrf.matter.commission.CommissionHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class IosCommissionHandler(
    private val onCommission: () -> Unit
) : CommissionHandler {
    override fun onCommissioningStarted() {
        onCommission()
    }
}

fun MainViewController(swiftCodeProvider: SwiftCodeProvider) =
    ComposeUIViewController {
        // Initialize koin
        initKoin (
            module {
                single { swiftCodeProvider }
            }
        )

        // Initialize Napier for logging
        Napier.base(DebugAntilog())
        Napier.i("Napier log initiated")

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            IosAppRoot()
        }

    }

sealed interface ScreenState {
    data object Initial : ScreenState
    data object Commissioning : ScreenState

    data object Error : ScreenState
}

private var isStarted = false

@Composable
fun IosAppRoot() {
    val commissioningViewModel: CommissioningViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()
    val state = remember { mutableStateOf<ScreenState>(ScreenState.Initial) }

    val commissionHandler = remember {
        IosCommissionHandler {
            state.value = ScreenState.Commissioning
            isStarted = true
        }
    }

    Napier.i("State: ${state.value}")
    LaunchedEffect(state.value) {
        (state.value as? ScreenState.Commissioning)?.let {
            delay(1000)

            val device = commissioningViewModel.startIosCommissioning {
                state.value = ScreenState.Error
            }
            device?.let {
                state.value = ScreenState.Initial
                homeViewModel.addCommissionedDevice(it, true, false)
            }
        }
    }

    CompositionLocalProvider(
        LocalCommissionHandler provides commissionHandler
    ) {
        when (state.value) {
            is ScreenState.Initial -> App(homeViewModel)
            is ScreenState.Error -> ErrorScreen()
            is ScreenState.Commissioning -> CommissioningScreen()
        }
    }
}

@Composable
fun CommissioningScreen() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ErrorScreen() {
    Box(Modifier.fillMaxSize()) {
        Text("Error occurred.", modifier = Modifier.align(Alignment.Center))
    }
}
