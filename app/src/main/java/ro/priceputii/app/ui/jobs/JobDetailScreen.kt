package ro.priceputii.app.ui.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.priceputii.app.R
import ro.priceputii.app.data.Catalog
import ro.priceputii.app.data.Interest
import ro.priceputii.app.data.Job
import ro.priceputii.app.data.JobContact
import ro.priceputii.app.data.JobStatus
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.data.rankSummary
import ro.priceputii.app.ui.components.ButtonProgress
import ro.priceputii.app.ui.components.EmptyState
import ro.priceputii.app.ui.components.Illustration
import ro.priceputii.app.ui.components.LoadingBox
import ro.priceputii.app.ui.components.RankBadge
import ro.priceputii.app.ui.components.dial
import ro.priceputii.app.ui.components.formatLei
import ro.priceputii.app.ui.components.sms
import ro.priceputii.app.ui.components.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    user: UserProfile,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    vm: JobDetailViewModel = viewModel(),
) {
    val job by vm.job.collectAsStateWithLifecycle()
    val interests by vm.interests.collectAsStateWithLifecycle()
    val contact by vm.contact.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lucrare") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                },
            )
        },
    ) { padding ->
        val current = job
        when {
            current == null -> LoadingBox(Modifier.padding(padding))
            current.id.isEmpty() -> EmptyState(
                "Lucrarea nu mai există",
                "Probabil a fost ștearsă de cel care a postat-o.",
                Modifier.padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                JobSummary(current)
                Spacer(Modifier.height(20.dp))
                val chatWith = { otherId: String, otherName: String ->
                    vm.openChat(user, otherId, otherName, current.task, onOpenChat)
                }
                if (current.ownerId == user.uid) {
                    OwnerSection(current, contact, interests, busy = vm.busy, onChat = chatWith, onMarkDone = vm::markDone)
                } else {
                    PriceputSection(
                        job = current,
                        user = user,
                        contact = contact,
                        busy = vm.busy,
                        onInterested = { vm.expressInterest(user) },
                        onChat = { chatWith(current.ownerId, current.ownerName) },
                    )
                }
                vm.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun JobSummary(job: Job) {
    Text(job.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Text(job.task.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineSmall)
    Text(
        formatLei(job.price),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(top = 4.dp),
    )
    if (job.status == JobStatus.DONE) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Illustration(R.drawable.ill_job_done, height = 180.dp)
            Text("Lucrarea a fost finalizată.", modifier = Modifier.padding(12.dp))
        }
    }
    Spacer(Modifier.height(16.dp))
    InfoRow("Zona", "${job.zone}, ${job.city}")
    if (job.whenText.isNotBlank()) InfoRow("Când", job.whenText)
    if (job.category == Catalog.TRANSPORT_CATEGORY) {
        if (job.fromPlace.isNotBlank()) InfoRow("De la", job.fromPlace)
        if (job.toPlace.isNotBlank()) InfoRow("Până la", job.toPlace)
    }
    InfoRow("Postată", "${timeAgo(job.createdAt)}, de ${job.ownerName}")
    if (job.description.isNotBlank()) {
        Text("Descriere", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
        Text(job.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(76.dp),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PriceputSection(
    job: Job,
    user: UserProfile,
    contact: JobContact?,
    busy: Boolean,
    onInterested: () -> Unit,
    onChat: () -> Unit,
) {
    val context = LocalContext.current
    val interested = user.uid in job.interestedIds
    if (interested) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Te-ai oferit pentru lucrare", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${job.ownerName} a primit numele și telefonul tău și te poate suna. Poți să-l suni și tu:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (contact != null) {
                    if (contact.address.isNotBlank()) {
                        Text("Adresa: ${contact.address}", modifier = Modifier.padding(top = 8.dp))
                    }
                    if (contact.phone.isNotBlank()) {
                        Text("Telefon: ${contact.phone}", modifier = Modifier.padding(top = 4.dp))
                        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { context.dial(contact.phone) }) { Text("Sună") }
                            OutlinedButton(onClick = { context.sms(contact.phone) }) { Text("SMS") }
                        }
                    }
                }
            }
        }
    } else if (job.status == JobStatus.OPEN) {
        if (user.isPriceput) {
            Button(
                onClick = onInterested,
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (busy) ButtonProgress() else Text("Sunt interesat de lucrarea dvs.")
            }
            Text(
                "${job.ownerName} primește imediat numele și telefonul tău, iar tu vezi adresa exactă.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        } else {
            Text(
                "Ca să te oferi pentru lucrări, activează rolul Priceput din Profil.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    OutlinedButton(
        onClick = onChat,
        enabled = !busy,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp),
    ) {
        Text("Trimite mesaj")
    }
}

@Composable
private fun OwnerSection(
    job: Job,
    contact: JobContact?,
    interests: List<Interest>,
    busy: Boolean,
    onChat: (String, String) -> Unit,
    onMarkDone: () -> Unit,
) {
    val context = LocalContext.current
    var confirmDone by remember { mutableStateOf(false) }

    if (contact != null && contact.address.isNotBlank()) {
        Text(
            "Adresa (o văd doar Priceputii care se oferă): ${contact.address}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
    }
    Text("Priceputi interesați (${interests.size})", style = MaterialTheme.typography.titleMedium)
    if (interests.isEmpty()) {
        if (job.status == JobStatus.OPEN) {
            Illustration(R.drawable.ill_find_priceputi, Modifier.padding(top = 8.dp), height = 170.dp)
        }
        Text(
            "Încă nu s-a oferit nimeni. Te anunțăm când apare cineva.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    interests.forEach { person ->
        Column(Modifier.padding(vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(person.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                RankBadge(person.starsTotal, person.ratingCount)
            }
            Text(
                "${rankSummary(person.starsTotal, person.ratingCount)} · ${person.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { context.dial(person.phone) }) { Text("Sună") }
                OutlinedButton(onClick = { context.sms(person.phone) }) { Text("SMS") }
                OutlinedButton(onClick = { onChat(person.priceputId, person.name) }, enabled = !busy) { Text("Mesaj") }
            }
        }
        HorizontalDivider()
    }
    if (job.status == JobStatus.OPEN) {
        OutlinedButton(
            onClick = { confirmDone = true },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp),
        ) {
            Text("Marchează finalizată")
        }
    }
    if (confirmDone) {
        AlertDialog(
            onDismissRequest = { confirmDone = false },
            title = { Text("Lucrarea e gata?") },
            text = { Text("Lucrarea dispare din lista Priceputilor. Plata se face direct între voi.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDone = false
                    onMarkDone()
                }) { Text("Da, e gata") }
            },
            dismissButton = { TextButton(onClick = { confirmDone = false }) { Text("Renunță") } },
        )
    }
}
