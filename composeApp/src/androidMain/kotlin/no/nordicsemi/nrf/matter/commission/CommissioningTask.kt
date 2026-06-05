package no.nordicsemi.nrf.matter.commission

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningRequest
import kotlinx.coroutines.channels.consumeEach
import no.nordicsemi.nrf.matter.home.HomeViewModelAndroid
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.service.AppCommissioningService
import org.koin.androidx.compose.koinViewModel

@Composable
actual fun CommissioningTask(onSuccess: (Device) -> Unit, onError: () -> Unit) {
    val homeViewModel: HomeViewModelAndroid = koinViewModel()

    val commissionDeviceLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                homeViewModel.gpsCommissioningDeviceSucceeded(result)
            } else {
                onError()
            }
        }

    LaunchedEffect(Unit) {
        homeViewModel.deviceEvent.consumeEach {
            onSuccess(it)
        }
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        commissionDevice(context, commissionDeviceLauncher)
    }
}

/**
 * Commission a device.
 */
private fun commissionDevice(
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
