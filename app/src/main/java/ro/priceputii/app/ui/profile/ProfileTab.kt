package ro.priceputii.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ro.priceputii.app.BuildConfig
import ro.priceputii.app.data.Roles
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.data.rankSummary
import ro.priceputii.app.ui.components.Avatar
import ro.priceputii.app.ui.components.AvatarPicker
import ro.priceputii.app.ui.components.RankBadge

@Composable
fun ProfileTab(
    user: UserProfile,
    onEditPriceputProfile: () -> Unit,
    onEditMyPrices: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var confirmSignOut by remember { mutableStateOf(false) }
    var pickAvatar by remember { mutableStateOf(false) }
    val roles = listOf(Roles.PRICEPUT to "Sunt Priceput", Roles.CLIENT to "Am o lucrare")

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Avatar(
            user.avatar,
            user.displayName,
            size = 88.dp,
            modifier = Modifier.clickable { pickAvatar = true },
        )
        TextButton(onClick = { pickAvatar = true }) { Text("Schimbă avatarul") }
        Text(user.displayName, style = MaterialTheme.typography.headlineSmall)
        Text(
            ServiceLocator.auth.currentPhone,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(user.city, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text("Folosesc aplicația ca", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            roles.forEachIndexed { index, (role, label) ->
                SegmentedButton(
                    selected = user.activeRole == role,
                    onClick = { scope.launch { runCatching { ServiceLocator.users.setActiveRole(user.uid, role) } } },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = roles.size),
                ) { Text(label) }
            }
        }

        if (user.isPriceput) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Profil de Priceput", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        RankBadge(user.starsTotal, user.ratingCount)
                    }
                    Text(
                        rankSummary(user.starsTotal, user.ratingCount),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "Zone: " + if (user.allCity) "tot orașul" else user.zones.joinToString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text("Distanță: până la ${user.radiusKm} km", style = MaterialTheme.typography.bodyMedium)
                    Text("Lucrări bifate: ${user.skills.size}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (user.hasPriceList) "Ai o listă de prețuri publică." else "Nu ai încă listă de prețuri.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(Modifier.padding(top = 12.dp)) {
                        OutlinedButton(onClick = onEditPriceputProfile) { Text("Editează") }
                        Button(onClick = onEditMyPrices, modifier = Modifier.padding(start = 8.dp)) {
                            Text("Lista mea de prețuri")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text(
            "Termeni și condiții · Politica de confidențialitate (în curând în aplicație)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = { confirmSignOut = true }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Ieși din cont")
        }
        Text(
            "Versiunea ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }

    if (pickAvatar) {
        var choice by remember { mutableStateOf(user.avatar) }
        AlertDialog(
            onDismissRequest = { pickAvatar = false },
            title = { Text("Alege-ți avatarul") },
            text = { AvatarPicker(selected = choice, onSelect = { choice = it }) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickAvatar = false
                        scope.launch { runCatching { ServiceLocator.users.setAvatar(user.uid, choice) } }
                    },
                    enabled = choice.isNotEmpty(),
                ) { Text("Salvează") }
            },
            dismissButton = { TextButton(onClick = { pickAvatar = false }) { Text("Renunță") } },
        )
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Ieși din cont?") },
            text = { Text("Poți intra din nou oricând cu numărul de telefon.") },
            confirmButton = { TextButton(onClick = { ServiceLocator.auth.signOut() }) { Text("Ieși") } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Renunță") } },
        )
    }
}
