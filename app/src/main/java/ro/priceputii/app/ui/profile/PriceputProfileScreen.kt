package ro.priceputii.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ro.priceputii.app.data.Catalog
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.data.searchKey
import ro.priceputii.app.ui.components.ButtonProgress
import kotlin.math.roundToInt

/** Where the Priceput works (zones, distance) and what they know how to do (tasks from the catalog). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PriceputProfileScreen(
    user: UserProfile,
    onboarding: Boolean,
    onDone: () -> Unit,
    onBack: (() -> Unit)?,
) {
    var allCity by remember { mutableStateOf(user.allCity) }
    val zones = remember { mutableStateListOf<String>().apply { addAll(user.zones) } }
    var radius by remember { mutableFloatStateOf(user.radiusKm.toFloat()) }
    val skills = remember { mutableStateListOf<String>().apply { addAll(user.skills) } }
    var zoneQuery by remember { mutableStateOf("") }
    var skillQuery by remember { mutableStateOf("") }
    val expanded = remember { mutableStateListOf<String>() }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun save() {
        error = when {
            skills.isEmpty() -> "Bifează cel puțin o lucrare pe care știi s-o faci."
            !allCity && zones.isEmpty() -> "Alege cel puțin o zonă sau „Tot orașul”."
            else -> null
        }
        if (error != null) return
        scope.launch {
            busy = true
            runCatching {
                ServiceLocator.users.savePriceputProfile(
                    uid = user.uid,
                    allCity = allCity,
                    zones = if (allCity) emptyList() else zones.toList(),
                    radiusKm = radius.roundToInt(),
                    skills = skills.toList(),
                )
            }.onSuccess { onDone() }.onFailure { error = "Nu am putut salva: ${it.localizedMessage}" }
            busy = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (onboarding) "Profilul tău de Priceput" else "Editează profilul") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Button(onClick = ::save, enabled = !busy, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        if (busy) ButtonProgress() else Text("Salvează (${skills.size} lucrări bifate)")
                    }
                }
            }
        },
    ) { padding ->
        val skillKey = skillQuery.searchKey().trim()
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Text("Orașul", style = MaterialTheme.typography.titleMedium)
                Text(user.city, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))

                Text("Unde iei lucrări", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Tot orașul ${user.city}", modifier = Modifier.weight(1f))
                    Switch(checked = allCity, onCheckedChange = { allCity = it })
                }
                if (!allCity) {
                    OutlinedTextField(
                        value = zoneQuery,
                        onValueChange = { zoneQuery = it },
                        placeholder = { Text("Caută cartier sau localitate") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    val zoneKey = zoneQuery.searchKey().trim()
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Catalog.zones
                            .filter { zoneKey.isEmpty() || zoneKey in it.searchKey() || it in zones }
                            .forEach { zone ->
                                FilterChip(
                                    selected = zone in zones,
                                    onClick = { if (zone in zones) zones.remove(zone) else zones.add(zone) },
                                    label = { Text(zone) },
                                )
                            }
                    }
                }

                Text(
                    "Până la ce distanță te deplasezi: ${radius.roundToInt()} km",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 20.dp),
                )
                Slider(value = radius, onValueChange = { radius = it }, valueRange = 1f..50f, steps = 48)

                Text("Ce știi să faci", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                Text(
                    "Primești lucrări doar din ce bifezi aici.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = skillQuery,
                    onValueChange = { skillQuery = it },
                    placeholder = { Text("Caută: lemne, pomi, zăpadă...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }

            Catalog.categories.forEach { category ->
                val tasks = if (skillKey.isEmpty()) {
                    category.tasks
                } else {
                    category.tasks.filter { skillKey in it.searchKey() }
                }
                if (tasks.isEmpty()) return@forEach
                val open = skillKey.isNotEmpty() || category.name in expanded
                val checked = category.tasks.count { it in skills }
                item(key = "c:" + category.name) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (category.name in expanded) expanded.remove(category.name) else expanded.add(category.name)
                            }
                            .padding(vertical = 14.dp),
                    ) {
                        Text(category.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        if (checked > 0) {
                            Text("$checked bifate", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                        }
                        Icon(
                            if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                        )
                    }
                    HorizontalDivider()
                }
                if (open) {
                    items(tasks, key = { "t:" + category.name + it }) { task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (task in skills) skills.remove(task) else skills.add(task) },
                        ) {
                            Checkbox(
                                checked = task in skills,
                                onCheckedChange = { on -> if (on) skills.add(task) else skills.remove(task) },
                            )
                            Text(task.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        }
    }
}
