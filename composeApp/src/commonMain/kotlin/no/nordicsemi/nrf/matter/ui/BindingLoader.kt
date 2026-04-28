package no.nordicsemi.nrf.matter.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.nrf.matter.theme.NordicTheme
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.binding_links_only
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BindingLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val signalPaths = listOf(
        "M141.56,54.5L141.56,30.5",
        "M124.59,61.53L107.62,44.56",
        "M117.56,78.5L93.56,78.5",
        "M189.56,150.5L189.56,174.5",
        "M206.53,143.47L223.5,160.44",
        "M213.56,126.5L237.56,126.5"
    )

    val signalAlphas = signalPaths.indices.map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 500,
                    delayMillis = index * 200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "signal_$index"
        )
    }

    val strokeColor = MaterialTheme.colorScheme.primary
    val strokeWidth = 8f
    val viewportWidth = 330f
    val viewportHeight = 205f

    Box(contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Res.drawable.binding_links_only),
            contentDescription = null,
        )

        Canvas(modifier = Modifier.matchParentSize()) {
            val scaleX = size.width / viewportWidth
            val scaleY = size.height / viewportHeight

            signalPaths.forEachIndexed { index, pathData ->
                val path = PathParser().parsePathString(pathData).toPath()
                val matrix = Matrix()
                matrix.scale(scaleX, scaleY)
                path.transform(matrix)

                drawPath(
                    path = path,
                    color = strokeColor.copy(alpha = signalAlphas[index].value),
                    style = Stroke(
                        width = strokeWidth * ((scaleX + scaleY) / 2f),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoaderPreview() {
    NordicTheme {
        BindingLoader()
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
