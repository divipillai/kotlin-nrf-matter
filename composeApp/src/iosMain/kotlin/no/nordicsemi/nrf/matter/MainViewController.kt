package no.nordicsemi.nrf.matter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import no.nordicsemi.nrf.matter.commission.CommissionHandler
import no.nordicsemi.nrf.matter.matter.MatterController
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

@Composable
fun IosAppRoot() {
    val showQrCodeScanner = remember { mutableStateOf(false) }
    val commissionHandler = remember {
        IosCommissionHandler {
            // Call into Swift / iOS commissioning logic
//            startIosCommissioning()
            showQrCodeScanner.value = true
        }
    }

    CompositionLocalProvider(
        LocalCommissionHandler provides commissionHandler
    ) {
        if (showQrCodeScanner.value) {
            QRCodeScanner()
        } else {
            App()
        }
    }
}

@Composable
fun QRCodeScanner() {
    QrScanner(
        modifier = Modifier,
        flashlightOn = true,
        cameraLens = CameraLens.Back,
        openImagePicker = false,
        onCompletion = {
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

fun startIosCommissioning() {
    // Matter commissioning on iOS
    Napier.d("iOS commissioning has started!")
    MatterController.commission()
}


