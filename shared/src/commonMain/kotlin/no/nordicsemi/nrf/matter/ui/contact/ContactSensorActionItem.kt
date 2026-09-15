package no.nordicsemi.nrf.matter.ui.contact

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContactSensorActionItem(
    isContactDetected: Boolean,
) {
    Icon(
        imageVector = if (isContactDetected) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
        contentDescription = if (isContactDetected) "Contact detected" else "No contact",
        tint = if (isContactDetected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
        modifier = Modifier.size(28.dp),
    )
}
