package no.nordicsemi.nrf.matter

import androidx.compose.material3.Surface
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController =
    ComposeUIViewController {
        Surface {
            App()
        }

    }
