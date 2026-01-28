package no.nordicsemi.nrf.matter

import androidx.compose.material3.Surface
import androidx.compose.ui.window.ComposeUIViewController
import no.nordicsemi.nrf.matter.ui.AppRoot
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController =
    ComposeUIViewController {
        Surface {
            AppRoot()
        }

    }
