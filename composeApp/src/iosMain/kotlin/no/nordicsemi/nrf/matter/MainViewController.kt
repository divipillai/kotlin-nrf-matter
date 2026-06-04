@file:OptIn(ExperimentalAtomicApi::class)

package no.nordicsemi.nrf.matter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.delay
import no.nordicsemi.nrf.matter.commission.CommissionHandler
import no.nordicsemi.nrf.matter.logger.NordicLogger
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.matter_loader
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.module
import platform.UIKit.UIViewController
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class IosCommissionHandler(
    private val onCommission: () -> Unit
) : CommissionHandler {
    override fun onCommissioningStarted() {
        onCommission()
    }
}

fun MainViewController(swiftCodeProvider: SwiftCodeProvider): UIViewController {
    NordicLogger.setLogger(swiftCodeProvider.getLogger())

    initKoin(
        module {
            single { swiftCodeProvider }
        }
    )

    return ComposeUIViewController {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            IosAppRoot()
        }
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

    NordicLogger.info("State: ${state.value}")
    LaunchedEffect(state.value) {
        (state.value as? ScreenState.Commissioning)?.let {
            delay(1000)

            try {
                val device = commissioningViewModel.startIosCommissioning()
                state.value = ScreenState.Initial
                homeViewModel.addCommissionedDevice(device, true, false)
            } catch (t: Throwable) {
                state.value = ScreenState.Error
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
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Image(
            painter = painterResource(resource = Res.drawable.matter_loader),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ErrorScreen() {
    Box(Modifier.fillMaxSize()) {
        Text("Error occurred.", modifier = Modifier.align(Alignment.Center))
    }
}
