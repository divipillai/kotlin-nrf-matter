package no.nordicsemi.nrf.matter.commission

import android.content.ComponentName
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningRequest
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import com.google.android.gms.home.matter.commissioning.MatterCommissioningApiException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.first
import no.nordicsemi.nrf.matter.home.CommissioningViewModelAndroid
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.service.AppCommissioningService
import org.koin.androidx.compose.koinViewModel

@Composable
actual fun CommissioningTask(onSuccess: (Device) -> Unit, onError: (CommissioningException) -> Unit) {
    val commissioningModelAndroid: CommissioningViewModelAndroid = koinViewModel()

    val commissionDeviceLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            try {
                val commissioningResult = CommissioningResult.fromIntentSenderResult(result.resultCode, result.data)
                commissioningModelAndroid.gpsCommissioningDeviceSucceeded(commissioningResult)
            } catch (t: Throwable) {
                NordicLogger.error("Commissioning failed", t)
                onError(t.toCommissioningException(commissioningModelAndroid.nextNodeId.value!!))
            }
        }

    LaunchedEffect(Unit) {
        commissioningModelAndroid.deviceEvent.consumeEach {
            onSuccess(it)
        }
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val deviceId = commissioningModelAndroid.nextNodeId.first { it != null }!! // Wait until device id is loaded.
        commissionDevice(context, deviceId, commissionDeviceLauncher, onError)
    }
}

/**
 * Commission a device.
 */
private fun commissionDevice(
    context: Context,
    deviceId: DeviceId,
    commissionDeviceLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    onError: (CommissioningException) -> Unit
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
            NordicLogger.error("Commissioning failed", error)
            onError(error.toCommissioningException(deviceId))
        }
}

private fun Throwable.toCommissioningException(deviceId: DeviceId): CommissioningException {
    return when (this) {
        is MatterCommissioningApiException -> CommissioningException(
            deviceId,
            Stage.COMMISSIONING,
            this.errorDetails.googleErrorCode,
            this.message ?: ""
        )
        is ApiException -> CommissioningException(
            deviceId,
            Stage.COMMISSIONING,
            this.status.statusCode,
            this.message ?: ""
        )
        else -> CommissioningException.unknown(Stage.COMMISSIONING)
    }
}
