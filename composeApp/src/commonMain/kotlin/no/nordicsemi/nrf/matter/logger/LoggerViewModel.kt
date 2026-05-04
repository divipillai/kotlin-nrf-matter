package no.nordicsemi.nrf.matter.logger

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class LoggerViewModel(private val logger: PlatformLogger) : ViewModel() {

    private val scope = CoroutineScope( // todo
        SupervisorJob() + Dispatchers.Main
    )

    fun getLogs() = logger.getLogs().stateIn(scope, SharingStarted.Lazily, emptyList())
}
