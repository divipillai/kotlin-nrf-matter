package no.nordicsemi.nrf.matter.logger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerScreen(padding: PaddingValues) {
    val viewModel: LoggerViewModel = koinViewModel()
    val logs = viewModel.filteredLogs.collectAsStateWithLifecycle().value
    val searchText = viewModel.filter.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        val expanded = rememberSaveable { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 16.dp
            )
        ) {
            stickyHeader {
                SearchBar(
                    state = rememberSearchBarState(),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchText,
                            onQueryChange = {
                                viewModel.setSearch(it)
                            },
                            onSearch = {
                                viewModel.setSearch(it)
                                expanded.value = false
                            },
                            expanded = expanded.value,
                            onExpandedChange = { expanded.value = it },
                            placeholder = { Text("Search") }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                    )
            }

            items(logs.size) {
                LogItem(logs[it])
            }
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
