package no.nordicsemi.nrf.matter.commission

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.nrf.matter.MatterCommissioner
import no.nordicsemi.nrf.matter.domain.OperationResult
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.repository.DevicesRepository

class CommissioningViewModelIos(
    private val matterCommissioner: MatterCommissioner,
    private val devicesRepository: DevicesRepository,
) : ViewModel() {

    private val mutex = Mutex()

    suspend fun startIosCommissioning(): OperationResult<Device> {
        mutex.withLock {
            val deviceId = devicesRepository.incrementAndReturnLastDeviceId()
            NordicLogger.debug("startIosCommissioning: $this", tag = TAG)
            NordicLogger.debug("iOS commissioning has started!", tag = TAG)
            NordicLogger.debug("New device id: $deviceId", tag = TAG)
            return matterCommissioner.startIosCommissioning(deviceId)
        }
    }

    companion object {
        private const val TAG = "Commissioning"
    }
}
