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
import no.nordicsemi.nrf.matter.commission.CommissionHandler
import no.nordicsemi.nrf.matter.matter.MatterController
import no.nordicsemi.nrf.matter.model.Device
import org.koin.compose.getKoin
import qrscanner.CameraLens
import qrscanner.QrScanner

class IosCommissionHandler(
    private val onCommission: () -> Unit
) : CommissionHandler {
    override fun onCommissioningStarted() {
        onCommission()
    }
}

fun MainViewController() =
    ComposeUIViewController {
        // Initialize koin
        initKoin()

        // Initialize Napier for logging
        Napier.base(DebugAntilog())

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            IosAppRoot()
        }

    }

sealed interface ScreenState {
    object Initial : ScreenState
    data class Commissioning(val payload: String) : ScreenState
    object QrScanner : ScreenState
    object Error : ScreenState
}

@Composable
fun IosAppRoot() {
    val homeViewModel: HomeViewModel = getKoin().get()
    val state = remember { mutableStateOf<ScreenState>(ScreenState.Initial) }

    val commissionHandler = remember {
        IosCommissionHandler {
            state.value = ScreenState.QrScanner
        }
    }

    LaunchedEffect(state.value) {
        (state.value as? ScreenState.Commissioning)?.payload?.let {
            val device = startIosCommissioning(it) {
                state.value = ScreenState.Error
            }
            device?.let {
                homeViewModel.addCommissionedDevice(it, true, false)
            }
        }
    }

    CompositionLocalProvider(
        LocalCommissionHandler provides commissionHandler
    ) {
        when (state.value) {
            is ScreenState.Initial -> App()
            is ScreenState.QrScanner -> QRCodeScanner {
                state.value = ScreenState.Commissioning(it)
            }
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

@Composable
fun QRCodeScanner(onCompletion: (String) -> Unit) {
    QrScanner(
        modifier = Modifier,
        flashlightOn = false,
        cameraLens = CameraLens.Back,
        openImagePicker = false,
        onCompletion = {
            onCompletion(it)
            Napier.i("On completion $it")
        },
        imagePickerHandler = {
            Napier.i("Image picker handler $it")
        },
        onFailure = {
            Napier.i("On failure $it")
        }
    )
}

suspend fun startIosCommissioning(code: String, onError: () -> Unit): Device? {
    // Matter commissioning on iOS
    Napier.d("iOS commissioning has started!")
    return MatterController.commission(code, onError)
}


