package ro.priceputii.app.ui.jobs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ro.priceputii.app.data.Catalog
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.ui.components.ButtonProgress
import ro.priceputii.app.ui.components.PickerDialog
import ro.priceputii.app.ui.components.PickerField
import ro.priceputii.app.ui.components.PickerGroup

class PostJobViewModel : ViewModel() {
    var task by mutableStateOf("")
    var zone by mutableStateOf("")
    var address by mutableStateOf("")
    var description by mutableStateOf("")
    var whenText by mutableStateOf("")
    var fromPlace by mutableStateOf("")
    var toPlace by mutableStateOf("")
    var price by mutableStateOf("")
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val isTransport: Boolean get() = Catalog.categoryOf(task) == Catalog.TRANSPORT_CATEGORY

    fun publish(user: UserProfile, onPosted: (String) -> Unit) {
        error = null
        val lei = price.trim().toLongOrNull()
        error = when {
            task.isEmpty() -> "Alege ce fel de lucrare este."
            zone.isEmpty() -> "Alege zona."
            description.isBlank() -> "Scrie pe scurt ce e de făcut."
            lei == null || lei <= 0 -> "Scrie câți lei oferi."
            isTransport && (fromPlace.isBlank() || toPlace.isBlank()) -> "Scrie de unde și până unde e transportul."
            else -> null
        }
        if (error != null || lei == null) return
        viewModelScope.launch {
            busy = true
            runCatching {
                ServiceLocator.jobs.post(
                    owner = user,
                    ownerPhone = ServiceLocator.auth.currentPhone,
                    task = task,
                    zone = zone,
                    address = address.trim(),
                    description = description.trim(),
                    whenText = whenText.trim(),
                    fromPlace = if (isTransport) fromPlace.trim() else "",
                    toPlace = if (isTransport) toPlace.trim() else "",
                    price = lei,
                )
            }.onSuccess(onPosted).onFailure { error = "Nu am putut publica: ${it.localizedMessage}" }
            busy = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(
    user: UserProfile,
    onBack: () -> Unit,
    onPosted: (String) -> Unit,
    vm: PostJobViewModel = viewModel(),
) {
    var pickTask by remember { mutableStateOf(false) }
    var pickZone by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Postează lucrare") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            PickerField("Ce lucrare ai?", vm.task, "Alege din listă", onClick = { pickTask = true })
            Spacer(Modifier.height(12.dp))
            PickerField("Zona din ${user.city}", vm.zone, "Alege cartierul sau localitatea", onClick = { pickZone = true })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.address,
                onValueChange = { vm.address = it },
                label = { Text("Adresa exactă (opțional)") },
                supportingText = { Text("O văd doar Priceputii care se oferă.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (vm.isTransport) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.fromPlace,
                    onValueChange = { vm.fromPlace = it },
                    label = { Text("De unde") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = vm.toPlace,
                    onValueChange = { vm.toPlace = it },
                    label = { Text("Până unde") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = vm.description,
                onValueChange = { vm.description = it },
                label = { Text("Descriere") },
                placeholder = { Text("Ex.: Am 3 pomi de tăiat, 2 meri și un cireș.") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.whenText,
                onValueChange = { vm.whenText = it },
                label = { Text(if (vm.isTransport) "Data și ora" else "Când să fie gata") },
                placeholder = { Text("Ex.: sâmbătă dimineață, săptămâna asta") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.price,
                onValueChange = { input -> vm.price = input.filter { it.isDigit() }.take(6) },
                label = { Text("Cât oferi") },
                suffix = { Text("lei") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }
            Button(
                onClick = { vm.publish(user, onPosted) },
                enabled = !vm.busy,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp),
            ) {
                if (vm.busy) ButtonProgress() else Text("Publică")
            }
            Text(
                "Priceputii din zona ta care știu să facă asta văd lucrarea și se pot oferi. Plata se face direct între voi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    if (pickTask) {
        PickerDialog(
            title = "Ce lucrare ai?",
            groups = Catalog.categories.map { PickerGroup(it.name, it.tasks) },
            onPick = {
                vm.task = it
                pickTask = false
            },
            onDismiss = { pickTask = false },
        )
    }
    if (pickZone) {
        PickerDialog(
            title = "Zona",
            groups = listOf(PickerGroup(null, Catalog.zones)),
            onPick = {
                vm.zone = it
                pickZone = false
            },
            onDismiss = { pickZone = false },
        )
    }
}
