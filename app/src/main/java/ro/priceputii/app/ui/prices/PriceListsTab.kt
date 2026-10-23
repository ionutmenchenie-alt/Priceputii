package ro.priceputii.app.ui.prices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ro.priceputii.app.R
import ro.priceputii.app.data.Catalog
import ro.priceputii.app.data.PriceEntry
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.data.rankSummary
import ro.priceputii.app.ui.components.Avatar
import ro.priceputii.app.ui.components.EmptyState
import ro.priceputii.app.ui.components.LoadingBox
import ro.priceputii.app.ui.components.PickerDialog
import ro.priceputii.app.ui.components.PickerField
import ro.priceputii.app.ui.components.PickerGroup
import ro.priceputii.app.ui.components.RankBadge
import ro.priceputii.app.ui.components.dial
import ro.priceputii.app.ui.components.formatLei
import ro.priceputii.app.ui.components.sms

@OptIn(ExperimentalCoroutinesApi::class)
class PriceListsViewModel : ViewModel() {
    val task = MutableStateFlow("")

    val results: StateFlow<List<PriceEntry>?> = task.flatMapLatest { task ->
        if (task.isEmpty()) flowOf(emptyList()) else ServiceLocator.prices.observeForTask(task, Catalog.CITY)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun openChat(user: UserProfile, entry: PriceEntry, onOpened: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                ServiceLocator.chats.openChat(user, entry.priceputId, entry.name, entry.avatar, entry.task)
            }.onSuccess(onOpened)
        }
    }
}

/** Clients pick a task and compare what the Priceputi ask for it, cheapest first. */
@Composable
fun PriceListsTab(
    user: UserProfile,
    onOpenChat: (String) -> Unit,
    onEditMyPrices: () -> Unit,
    modifier: Modifier = Modifier,
    vm: PriceListsViewModel = viewModel(),
) {
    val task by vm.task.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    var picking by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        Text(
            "Prețuri în ${user.city}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )
        PickerField(
            label = "Lucrarea",
            value = task,
            placeholder = "Alege lucrarea ca să vezi prețurile",
            onClick = { picking = true },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (user.isPriceput) {
            TextButton(onClick = onEditMyPrices, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(if (user.hasPriceList) "Editează lista mea de prețuri" else "Fă-ți lista ta de prețuri")
            }
        }

        val list = results
        when {
            task.isEmpty() -> EmptyState(
                "Compară prețurile",
                "Alege o lucrare și vezi cât cer Priceputii din oraș, de la cel mai ieftin.",
                image = R.drawable.ill_find_priceputi,
            )
            list == null -> LoadingBox()
            list.isEmpty() -> EmptyState(
                "Încă nu are nimeni preț pentru asta",
                "Poți posta lucrarea și Priceputii îți scriu ei.",
                image = R.drawable.ill_find_priceputi,
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(list, key = { it.id }) { entry ->
                    PriceCard(
                        entry = entry,
                        isMe = entry.priceputId == user.uid,
                        onMessage = { vm.openChat(user, entry, onOpenChat) },
                    )
                }
            }
        }
    }

    if (picking) {
        PickerDialog(
            title = "Ce lucrare?",
            groups = Catalog.categories.map { PickerGroup(it.name, it.tasks) },
            onPick = {
                vm.task.value = it
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun PriceCard(entry: PriceEntry, isMe: Boolean, onMessage: () -> Unit) {
    val context = LocalContext.current
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(entry.avatar, entry.name)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(entry.name + if (isMe) " (tu)" else "", style = MaterialTheme.typography.titleMedium)
                    Text(
                        rankSummary(entry.starsTotal, entry.ratingCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                RankBadge(entry.starsTotal, entry.ratingCount)
            }
            Text(
                "${formatLei(entry.price)} / ${entry.unit}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 10.dp),
            )
            if (!isMe && entry.phone.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                ) {
                    Button(onClick = { context.dial(entry.phone) }) { Text("Sună") }
                    OutlinedButton(onClick = { context.sms(entry.phone) }) { Text("SMS") }
                    OutlinedButton(onClick = onMessage) { Text("Mesaj") }
                }
            }
        }
    }
}
