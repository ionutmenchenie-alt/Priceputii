package ro.priceputii.app.ui.prices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ro.priceputii.app.R
import ro.priceputii.app.data.Catalog
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.ui.components.ButtonProgress
import ro.priceputii.app.ui.components.EmptyState
import ro.priceputii.app.ui.components.LoadingBox

/** A Priceput sets a price and unit for each task they ticked in their profile. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPriceListScreen(user: UserProfile, onBack: () -> Unit, onEditSkills: () -> Unit) {
    val flow = remember(user.uid) { ServiceLocator.prices.observeMine(user.uid) }
    val saved by flow.collectAsStateWithLifecycle(initialValue = null)
    val prices = remember { mutableStateMapOf<String, String>() }
    val units = remember { mutableStateMapOf<String, String>() }
    var loaded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(saved) {
        val list = saved ?: return@LaunchedEffect
        if (!loaded) {
            list.forEach {
                prices[it.task] = it.price.toString()
                units[it.task] = it.unit
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista mea de prețuri") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                },
            )
        },
    ) { padding ->
        when {
            user.skills.isEmpty() -> Column(Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    "Bifează întâi ce lucrări faci",
                    "Apoi revino aici și pune câte un preț pentru fiecare.",
                    image = R.drawable.ill_priceput_jobs,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onEditSkills, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)) {
                    Text("Alege lucrările")
                }
            }
            !loaded -> LoadingBox(Modifier.padding(padding))
            else -> Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    "Pune prețul pentru lucrările pe care vrei să apară. Lasă gol unde nu vrei să afișezi preț.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Clienții văd numele, telefonul și prețurile tale.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                user.skills.forEach { task ->
                    PriceRow(
                        task = task,
                        price = prices[task].orEmpty(),
                        unit = units[task] ?: Catalog.units.first(),
                        onPrice = { value -> prices[task] = value.filter { it.isDigit() }.take(6) },
                        onUnit = { units[task] = it },
                    )
                    Spacer(Modifier.height(10.dp))
                }
                message?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
                }
                Button(
                    onClick = {
                        message = null
                        busy = true
                        val entries = user.skills.mapNotNull { task ->
                            val price = prices[task]?.toLongOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
                            task to (price to (units[task] ?: Catalog.units.first()))
                        }.toMap()
                        scope.launch {
                            message = runCatching {
                                ServiceLocator.prices.saveMine(user, ServiceLocator.auth.currentPhone, entries, saved.orEmpty())
                            }.fold(
                                onSuccess = { if (entries.isEmpty()) "Lista a fost ștearsă." else "Lista a fost salvată." },
                                onFailure = { "Nu am putut salva: ${it.localizedMessage}" },
                            )
                            busy = false
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp),
                ) {
                    if (busy) ButtonProgress() else Text("Salvează lista")
                }
                OutlinedButton(onClick = onEditSkills, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Adaugă sau scoate lucrări")
                }
            }
        }
    }
}

@Composable
private fun PriceRow(task: String, price: String, unit: String, onPrice: (String) -> Unit, onUnit: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(task.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = onPrice,
                    label = { Text("Preț (lei)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                Text("lei /")
                Box {
                    OutlinedButton(onClick = { menu = true }, modifier = Modifier.width(120.dp)) {
                        Text(unit, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        Catalog.units.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { onUnit(option); menu = false })
                        }
                    }
                }
            }
        }
    }
}
