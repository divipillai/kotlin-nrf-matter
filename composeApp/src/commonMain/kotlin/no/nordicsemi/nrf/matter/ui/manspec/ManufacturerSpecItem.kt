package no.nordicsemi.nrf.matter.ui.manspec

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.domain.ManufacturerSpecificData
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.theme.NordicRed
import no.nordicsemi.nrf.matter.theme.NordicSun
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.light_bulb
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ManufacturerSpecItem(
    device: DeviceUiModel,
    manufacturerSpecificData: ManufacturerSpecificData,
    isLedOn: UiState<Boolean>,
    isButtonOn: UiState<Boolean>,
    randomNumber: UiState<Int>,
    setLed: (Boolean) -> Unit,
    generateRandomNumber: () -> Unit,
    onClick: () -> Unit
) {
    Column {
        DeviceItemContainer(
            device = device,
            manufacturerSpecificData = manufacturerSpecificData,
            randomNumber = randomNumber,
            generateRandomNumber = generateRandomNumber,
            onDeviceClick = onClick,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = (isLedOn as? UiState.Success)?.data ?: false,
                    onCheckedChange = setLed
                )

                Text("LED", style = MaterialTheme.typography.labelSmall)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = (isButtonOn as? UiState.Success)?.data ?: false,
                    onCheckedChange = { /* disabled */ },
                    enabled = false,
                )

                Text("Button", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DeviceItemContainer(
    device: DeviceUiModel,
    manufacturerSpecificData: ManufacturerSpecificData,
    randomNumber: UiState<Int>,
    onDeviceClick: () -> Unit,
    generateRandomNumber: () -> Unit,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = if (device.isOnline) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(0.3f)
        ) else CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onDeviceClick() }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val boxColor = if (device.isOnline)
                    NordicSun
                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            boxColor,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(Res.drawable.light_bulb),
                        contentDescription = null,
                        tint = if (device.isOnline)
                            MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = manufacturerSpecificData.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "Turn light ON or OFF",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(0.5f)
                    )
                }

                content()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { generateRandomNumber() }) {
                    Text("Generate number")
                }

                Spacer(modifier = Modifier.padding(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.height(40.dp)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        when (randomNumber) {
                            is UiState.Error -> Icon(Icons.Default.Error, null, tint = NordicRed, modifier = Modifier.size(16.dp))
                            is UiState.Idle<Int> -> Text("__")
                            is UiState.Loading<Int> -> CircularProgressIndicator(Modifier.size(16.dp))
                            is UiState.Success<Int> -> Text("${randomNumber.data}")
                        }
                    }
                    Text("Random number", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
