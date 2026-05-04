package no.nordicsemi.nrf.matter.logger

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoggerScreen() {
    val viewModel: LoggerViewModel = koinViewModel()
    val logs = viewModel.getLogs().collectAsStateWithLifecycle().value

    logs.forEach { LogItem(it) }
}

@Composable
private fun LogItem(item: LogEntity) {
    Column {
        Text(item.tag)

        Text(item.message)
    }
}
