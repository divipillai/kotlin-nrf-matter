package no.nordicsemi.nrf.matter.commission

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.nrf.matter.SwiftCodeProvider
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device

class CommissioningViewModel(
    private val swiftCodeProvider: SwiftCodeProvider
) : ViewModel() {

    private val mutex = Mutex()

    suspend fun startIosCommissioning(): Device {
        mutex.withLock {
            NordicLogger.debug("startIosCommissioning: $this")
            NordicLogger.debug("iOS commissioning has started!")
            return swiftCodeProvider.getMatterCommissioner().startIosCommissioning()
        }
    }
}
