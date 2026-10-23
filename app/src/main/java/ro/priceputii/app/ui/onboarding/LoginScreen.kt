package ro.priceputii.app.ui.onboarding

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.priceputii.app.R
import ro.priceputii.app.data.Roles
import ro.priceputii.app.ui.components.AvatarPicker
import ro.priceputii.app.ui.components.Illustration
import ro.priceputii.app.ui.components.ButtonProgress
import ro.priceputii.app.ui.components.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onBack: () -> Unit, vm: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vm.role == Roles.PRICEPUT) "Înregistrare Priceput" else "Înregistrare") },
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
                .padding(24.dp),
        ) {
            when (vm.step) {
                LoginViewModel.Step.DETAILS, LoginViewModel.Step.NAMES_ONLY -> {
                    Illustration(
                        if (vm.role == Roles.PRICEPUT) R.drawable.ill_login_garden else R.drawable.ill_find_priceputi,
                        Modifier.padding(bottom = 20.dp),
                        height = 180.dp,
                    )
                    Text("Alege-ți avatarul", style = MaterialTheme.typography.titleMedium)
                    AvatarPicker(
                        selected = vm.avatar,
                        onSelect = { vm.avatar = it },
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                    )
                    OutlinedTextField(
                        value = vm.firstName,
                        onValueChange = { vm.firstName = it },
                        label = { Text("Prenume") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = vm.lastName,
                        onValueChange = { vm.lastName = it },
                        label = { Text("Nume") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (vm.step == LoginViewModel.Step.DETAILS) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = vm.phone,
                            onValueChange = { vm.phone = it },
                            label = { Text("Telefon") },
                            placeholder = { Text("07xx xxx xxx") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Îți trimitem un cod prin SMS. Nu ai nevoie de parolă.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (vm.step == LoginViewModel.Step.NAMES_ONLY) {
                                vm.completeRegistration()
                            } else {
                                context.findActivity()?.let(vm::sendCode)
                            }
                        },
                        enabled = !vm.busy,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        if (vm.busy) {
                            ButtonProgress()
                        } else {
                            Text(if (vm.step == LoginViewModel.Step.NAMES_ONLY) "Continuă" else "Trimite codul")
                        }
                    }
                }

                LoginViewModel.Step.CODE -> {
                    Text("Am trimis un cod prin SMS la ${vm.phone}.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = vm.code,
                        onValueChange = { input -> vm.code = input.filter { it.isDigit() }.take(6) },
                        label = { Text("Codul din SMS") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = vm::confirmCode,
                        enabled = !vm.busy,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        if (vm.busy) {
                            ButtonProgress()
                        } else {
                            Text("Intră în cont")
                        }
                    }
                    TextButton(onClick = vm::changeNumber, modifier = Modifier.fillMaxWidth()) {
                        Text("Schimbă numărul")
                    }
                }
            }

            vm.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Text(
                "Continuând, ești de acord cu Termenii și condițiile și cu Politica de confidențialitate Priceputii.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 32.dp),
            )
        }
    }
}
