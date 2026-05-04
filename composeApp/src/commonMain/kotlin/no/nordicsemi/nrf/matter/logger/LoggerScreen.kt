package no.nordicsemi.nrf.matter.logger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.size(16.dp)) }

            items(logs.size) {
                LogItem(logs[it])
            }
            
            item { Spacer(modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
private fun LogItem(item: LogEntity) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.message)

            Text(item.formattedDate.value, style = MaterialTheme.typography.labelSmall)

            Text(item.tag)
        }
    }
}
