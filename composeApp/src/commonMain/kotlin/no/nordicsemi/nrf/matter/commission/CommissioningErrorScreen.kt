package no.nordicsemi.nrf.matter.commission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.nrf.matter.theme.BorderDark
import no.nordicsemi.nrf.matter.theme.BorderLight
import no.nordicsemi.nrf.matter.theme.CardDark
import no.nordicsemi.nrf.matter.theme.CardLight
import no.nordicsemi.nrf.matter.theme.ErrorBannerBgDark
import no.nordicsemi.nrf.matter.theme.ErrorBannerBgLight
import no.nordicsemi.nrf.matter.theme.ErrorDescDark
import no.nordicsemi.nrf.matter.theme.ErrorDescLight
import no.nordicsemi.nrf.matter.theme.ErrorTitleDark
import no.nordicsemi.nrf.matter.theme.ErrorTitleLight
import no.nordicsemi.nrf.matter.theme.NordicRed
import no.nordicsemi.nrf.matter.theme.PillBgDark
import no.nordicsemi.nrf.matter.theme.PillBgLight
import no.nordicsemi.nrf.matter.theme.SlatePrimary
import no.nordicsemi.nrf.matter.theme.TextBodyDark
import no.nordicsemi.nrf.matter.theme.TextBodyLight
import no.nordicsemi.nrf.matter.theme.TextTitleDark
import no.nordicsemi.nrf.matter.theme.TextTitleLight

@Composable
fun CommissioningErrorScreen(onBack: () -> Unit, navigateToLogs: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ErrorBanner(isSystemInDarkTheme())

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Connection Failed",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSystemInDarkTheme()) TextTitleDark else TextTitleLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    append("We were unable to pair the ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("RetroBulb Smart Gen 2")
                    }
                    append(" to your network.")
                },
                fontSize = 16.sp,
                color = if (isSystemInDarkTheme()) TextBodyDark else TextBodyLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            DetailsCard(isSystemInDarkTheme())

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TROUBLESHOOTING",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = if (isSystemInDarkTheme()) TextBodyDark else TextBodyLight,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TroubleshootItem(
                    text = "Ensure the device is in commissioning mode (fast blinking light).",
                    isDark = isSystemInDarkTheme()
                )
                Spacer(modifier = Modifier.height(12.dp))
                TroubleshootItem(
                    text = "Verify that the Fabric ID 0x2A19F8 is correctly configured.",
                    isDark = isSystemInDarkTheme()
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = SlatePrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Commissioning", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = { },
                color = if (isSystemInDarkTheme()) PillBgDark else PillBgLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Terminal,
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) TextTitleDark else TextTitleLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Go to Logs Panel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) TextTitleDark else TextTitleLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun NavLabel(text: String, color: Color = Color.Unspecified) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        color = color,
        maxLines = 1
    )
}

@Composable
fun ErrorBanner(isDark: Boolean) {
    Surface(
        color = if (isDark) ErrorBannerBgDark else ErrorBannerBgLight,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) CardDark else CardLight)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LinkOff,
                    contentDescription = "Error",
                    tint = NordicRed,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Commissioning Error",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) ErrorTitleDark else ErrorTitleLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The Matter node (0x1034) could not be reached. The process timed out while establishing the secure channel over Fabric Index 1.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (isDark) ErrorDescDark else ErrorDescLight
                )
            }
        }
    }
}

@Composable
fun DetailsCard(isDark: Boolean) {
    Surface(
        color = if (isDark) CardDark else CardLight,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
        ) {
            DetailRow(
                label = "Error Code",
                value = {
                    Surface(
                        color = if (isDark) PillBgDark else PillBgLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "0x00000032",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDark) TextTitleDark else TextTitleLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                },
                isDark = isDark
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow(
                label = "Reason",
                value = {
                    Text(
                        text = "Network Timeout (MLE)",
                        fontSize = 14.sp,
                        color = if (isDark) TextTitleDark else TextTitleLight
                    )
                },
                isDark = isDark
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow(
                label = "Protocol",
                value = {
                    Text(
                        text = "Project CHIP JNI",
                        fontSize = 14.sp,
                        color = if (isDark) TextTitleDark else TextTitleLight
                    )
                },
                isDark = isDark
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: @Composable () -> Unit, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isDark) TextBodyDark else TextBodyLight
        )
        value()
    }
}

@Composable
fun TroubleshootItem(text: String, isDark: Boolean) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Rounded.CheckCircleOutline,
            contentDescription = null,
            tint = SlatePrimary,
            modifier = Modifier.size(20.dp).offset(y = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = if (isDark) TextBodyDark else TextBodyLight
        )
    }
}
