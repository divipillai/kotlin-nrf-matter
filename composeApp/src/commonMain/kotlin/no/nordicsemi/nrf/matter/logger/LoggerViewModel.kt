package no.nordicsemi.nrf.matter.logger

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LoggerViewModel(private val logger: PlatformLogger) : ViewModel() {

    private val scope = CoroutineScope( // todo
        SupervisorJob() + Dispatchers.Main
    )

    private val logs = logger.getLogs()
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    val filter = MutableStateFlow("")

    val filteredLogs = logs.combine(filter) { logs, filter ->
        logs.filter { it.message.lowercase().contains(filter.lowercase()) }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())

    fun setSearch(value: String) {
        filter.value = value
    }
}
