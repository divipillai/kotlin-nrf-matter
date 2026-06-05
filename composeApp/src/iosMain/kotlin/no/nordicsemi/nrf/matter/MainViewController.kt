@file:OptIn(ExperimentalAtomicApi::class)

package no.nordicsemi.nrf.matter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import no.nordicsemi.nrf.matter.logger.NordicLogger
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.module
import platform.UIKit.UIViewController
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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
            App(koinViewModel())
        }
    }
}
