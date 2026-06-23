package no.nordicsemi.nrf.matter


import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import no.nordicsemi.nrf.matter.app.R
import org.koin.androidx.compose.koinViewModel

abstract class NordicActivity : ComponentActivity() {

    companion object {
        private var coldStart = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.NordicTheme)
        super.onCreate(savedInstanceState)

        setContent {
            App(homeViewModel = koinViewModel())
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        val splashScreen = installSplashScreen()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (coldStart) {
                coldStart = false
                val then = System.currentTimeMillis()
                splashScreen.setKeepOnScreenCondition {
                    val now = System.currentTimeMillis()
                    now < then + 900
                }
            }
        } else {
            splashScreen.setKeepOnScreenCondition { true }
        }
    }
}