package no.nordicsemi.nrf.matter

import android.os.Bundle
import androidx.activity.compose.setContent
import no.nordicsemi.nrf.matter.theme.NordicActivity
import no.nordicsemi.nrf.matter.ui.AppRoot

class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppRoot()
            /* Surface(
                 color = MaterialTheme.colorScheme.surface,
                 modifier = Modifier.fillMaxSize()
             ) {
                 *//* val navController = rememberNavController()
                 AppNavigationLayout(navController)*//*
            }*/
        }
    }
}