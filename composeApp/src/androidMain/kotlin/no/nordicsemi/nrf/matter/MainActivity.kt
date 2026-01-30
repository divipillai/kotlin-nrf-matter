package no.nordicsemi.nrf.matter

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import no.nordicsemi.nrf.matter.theme.NordicActivity
import no.nordicsemi.nrf.matter.ui.AppRoot

class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxSize()
            ) {
                AppRoot()
                /* val navController = rememberNavController()
                 AppNavigationLayout(navController)*/
            }
        }
    }
}