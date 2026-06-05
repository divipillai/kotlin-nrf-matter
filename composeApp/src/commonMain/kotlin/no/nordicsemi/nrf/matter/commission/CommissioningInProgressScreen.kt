package no.nordicsemi.nrf.matter.commission

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.matter_loader
import org.jetbrains.compose.resources.painterResource

@Composable
fun CommissioningInProgressScreen() {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Image(
            painter = painterResource(resource = Res.drawable.matter_loader),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
