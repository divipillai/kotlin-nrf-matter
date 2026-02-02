package no.nordicsemi.nrf.matter

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import no.nordicsemi.nrf.matter.commission.CommissionHandler
import no.nordicsemi.nrf.matter.home.HomeViewModelAndroid
import no.nordicsemi.nrf.matter.home.commissionDevice
import no.nordicsemi.nrf.matter.theme.NordicActivity
import org.koin.androidx.compose.koinViewModel

class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidAppRoot()
        }
    }
}

class AndroidCommissionHandler(
    private val onCommissionDevice: () -> Unit
) : CommissionHandler {
    override fun onCommissioningStarted() {
        onCommissionDevice()
    }
}


@Composable
fun AndroidAppRoot() {
    val homeViewModel: HomeViewModelAndroid = koinViewModel()

    val commissionDeviceLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                homeViewModel.gpsCommissioningDeviceSucceeded(result)
            } else {
                homeViewModel.commissionDeviceFailed(result.resultCode)
            }
        }

    val context = LocalContext.current

    val commissionHandler = remember {
        AndroidCommissionHandler {
            commissionDevice(
                context.applicationContext,
                commissionDeviceLauncher
            )
        }
    }

    CompositionLocalProvider(
        LocalCommissionHandler provides commissionHandler
    ) {
        App()
    }
}
