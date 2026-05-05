package no.nordicsemi.nrf.matter.logger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerScreen(padding: PaddingValues) {
    val viewModel: LoggerViewModel = koinViewModel()
    val logs = viewModel.filteredLogs.collectAsStateWithLifecycle().value
    val searchText = viewModel.filter.collectAsStateWithLifecycle().value
    val logLevel = viewModel.logLevel.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        val expanded = rememberSaveable { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 16.dp
            )
        ) {
            stickyHeader {
                val brush = remember {
                    Brush.linearGradient(
                        colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        state = rememberTextFieldState(), textStyle = TextStyle(brush = brush)
                    )
                    LogLevelPicker(logLevel) {
                        viewModel.setLogLevel(it)
                    }
                }
            }

            itemsIndexed(logs) { index, item ->
                LogItem(item)

                if (index < logs.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LogItem(item: LogEntity) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(item.formattedDate.value, style = MaterialTheme.typography.labelSmall)

            Text(item.tag, style = MaterialTheme.typography.labelSmall)

            LevelItem(item.level)
        }

        Text(item.message, color = item.level.toColor())
    }
}

@Composable
private fun LevelItem(level: LogLevel) {
    Text(
        text = level.toName(),
        modifier = Modifier
            .background(
                color = level.toColor(),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(4.dp),
        style = MaterialTheme.typography.labelSmall,
    )
}

private fun LogLevel.toName() = when (this) {
    LogLevel.INFO -> "info"
    LogLevel.DEBUG -> "debug"
    LogLevel.ERROR -> "error"
}

private fun LogLevel.toColor() = when (this) {
    LogLevel.INFO -> Color(0xFF008d45)
    LogLevel.DEBUG -> Color(0xFF00A9CE)
    LogLevel.ERROR -> Color(0xFFBA1B1B)
}

@Composable
fun LogLevelPicker(logLevel: LogLevel, onChange: (LogLevel) -> Unit) {
    val expanded = remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .width(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = logLevel.toColor(),
                )
                .clickable { expanded.value = true }
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = logLevel.toName(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Icon(Icons.Default.ArrowDropDown, "Choose log level.")
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            LogLevel.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toName()) },
                    onClick = {
                        onChange(item)
                        expanded.value = false
                    }
                )
            }
        }
    }
}
