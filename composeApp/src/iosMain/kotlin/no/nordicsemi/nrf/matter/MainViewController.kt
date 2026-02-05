package no.nordicsemi.nrf.matter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import no.nordicsemi.nrf.matter.commission.CommissionHandler

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

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            IosAppRoot()
        }

    }

@Composable
fun IosAppRoot() {
    val commissionHandler = remember {
        IosCommissionHandler {
            // Call into Swift / iOS commissioning logic
            startIosCommissioning()
        }
    }

    CompositionLocalProvider(
        LocalCommissionHandler provides commissionHandler
    ) {
        App()
    }
}

fun startIosCommissioning() {
    // Matter commissioning on iOS
    print("iOS commissioning has started!")
}


