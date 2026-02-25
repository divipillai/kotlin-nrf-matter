package no.nordicsemi.nrf.matter

import androidx.lifecycle.ViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.nrf.matter.matter.MatterController
import no.nordicsemi.nrf.matter.model.Device

class CommissioningViewModel(
    private val swiftCodeProvider: SwiftCodeProvider
) : ViewModel() {

    private val mutex = Mutex()

    suspend fun startIosCommissioning(code: String, threadNetwork: ThreadNetwork?, onError: () -> Unit): Device? {
        mutex.withLock {
            Napier.d("startIosCommissioning: $this")
            Napier.d("iOS commissioning has started!")
            return MatterController.commission(code, threadNetwork, onError)
        }
    }
}