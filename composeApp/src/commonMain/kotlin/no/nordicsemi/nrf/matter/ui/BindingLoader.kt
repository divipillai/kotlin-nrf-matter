package no.nordicsemi.nrf.matter.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.binding_links_only
import nrfmatterformobile.composeapp.generated.resources.binding_signals_only
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BindingLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // The "Blink" animation for the outer lines
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink2"
    )

    Box(contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Res.drawable.binding_links_only),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer(alpha = alpha2)
        )

        Image(
            painter = painterResource(Res.drawable.binding_signals_only),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(alpha = alpha) // Only these will blink
        )
    }
}

@Composable
internal fun BindingLoaderDialog(
    loadingText: @Composable (() -> Unit) = {}
) {
    Dialog(
        onDismissRequest = { /* Do nothing */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BindingLoader()
            loadingText()
        }

    }
}
