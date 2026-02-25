package no.nordicsemi.nrf.matter

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningRequest
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import no.nordicsemi.nrf.matter.commission.CommissionHandler
import no.nordicsemi.nrf.matter.home.HomeViewModelAndroid
import no.nordicsemi.nrf.matter.service.AppCommissioningService
import no.nordicsemi.nrf.matter.theme.NordicActivity
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin

class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Napier.base(DebugAntilog())
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

    val commissionHandler = remember(context, commissionDeviceLauncher) {
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
        App(homeViewModel = getKoin().get())
    }
}


/**
 * Commission a device.
 */
fun commissionDevice(
    context: Context,
    commissionDeviceLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
) {
    val commissionDeviceRequest =
        CommissioningRequest.builder()
//            .setOnboardingPayload(payload) // Add device payload directly to commission a specific device, such as payload = "MT:6FCJ142C00KA0648G00"
            .setCommissioningService(ComponentName(context, AppCommissioningService::class.java))
            .build()

    Matter.getCommissioningClient(context)
        .commissionDevice(commissionDeviceRequest)
        .addOnSuccessListener { result ->
            commissionDeviceLauncher.launch(IntentSenderRequest.Builder(result).build())
        }
        .addOnFailureListener { error ->
            Log.e("AAA", error.message.toString())
        }
}
