package no.nordicsemi.nrf.matter.commission

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.nrf.matter.SwiftCodeProvider
import no.nordicsemi.nrf.matter.device.OperationResult
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.repository.DevicesRepository

class CommissioningViewModelIos(
    private val swiftCodeProvider: SwiftCodeProvider,
    private val devicesRepository: DevicesRepository,
) : ViewModel() {

    private val mutex = Mutex()

    suspend fun startIosCommissioning(): OperationResult<Device> {
        mutex.withLock {
            val deviceId = devicesRepository.incrementAndReturnLastDeviceId()
            NordicLogger.debug("startIosCommissioning: $this")
            NordicLogger.debug("iOS commissioning has started!")
            NordicLogger.debug("New device id: $deviceId")
            return swiftCodeProvider.getMatterCommissioner().startIosCommissioning(deviceId)
        }
    }
}
