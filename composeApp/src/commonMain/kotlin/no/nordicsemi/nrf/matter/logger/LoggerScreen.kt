package no.nordicsemi.nrf.matter.logger

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoggerScreen(padding: PaddingValues) {
    val viewModel: LoggerViewModel = koinViewModel()
    val logs = viewModel.logs.collectAsStateWithLifecycle().value

    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(logs.size) {
                LogItem(logs[it])
            }
        }
    }
}

@Composable
private fun LogItem(item: LogEntity) {
    Column {
        Text(item.tag)

        Text(item.message)
    }
}
