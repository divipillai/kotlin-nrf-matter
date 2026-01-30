package no.nordicsemi.nrf.matter.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.nordicsemi.nrf.matter.theme.dark_md_appBarColor
import no.nordicsemi.nrf.matter.theme.light_md_appBarColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    topAppBarTitle: String,
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = topAppBarTitle,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = if (isSystemInDarkTheme()) dark_md_appBarColor else light_md_appBarColor,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        windowInsets = WindowInsets.displayCutout
            .union(WindowInsets.statusBars)
            .union(WindowInsets.navigationBars)
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),

        navigationIcon = {
            IconButton(onClick = {
                // Navigate to Home screen.
            }) {
                Icon(
                    Icons.Filled.Home, contentDescription = "Home",
                )
            }
        },
        actions = {
            IconButton(onClick = {
                // onClick Settings
            }) {
                Icon(
                    Icons.Filled.Settings, contentDescription = "Settings",
                )
            }
        },
    )
}