package ro.priceputii.app.ui.jobs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ro.priceputii.app.R
import ro.priceputii.app.data.Job
import ro.priceputii.app.data.JobSort
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.data.filterJobs
import ro.priceputii.app.ui.components.EmptyState
import ro.priceputii.app.ui.components.LoadingBox

class JobListViewModel : ViewModel() {
    val jobs: StateFlow<List<Job>?> = ServiceLocator.jobs.observeOpenJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val query = MutableStateFlow("")
    val sort = MutableStateFlow(JobSort.NEWEST)
    val onlyMatching = MutableStateFlow(false)
}

@Composable
fun JobListTab(
    user: UserProfile,
    onOpenJob: (String) -> Unit,
    onEditMyPrices: () -> Unit,
    modifier: Modifier = Modifier,
    vm: JobListViewModel = viewModel(),
) {
    val jobs by vm.jobs.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val sort by vm.sort.collectAsStateWithLifecycle()
    val onlyMatching by vm.onlyMatching.collectAsStateWithLifecycle()
    val visible = remember(jobs, query, sort, onlyMatching, user) {
        jobs?.let { filterJobs(it, query, sort, onlyMatching, user) }
    }

    Column(modifier.fillMaxSize()) {
        Text(
            "Lucrări în ${user.city}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )
        if (user.actsAsPriceput && !user.hasPriceList) {
            Card(
                onClick = onEditMyPrices,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Fă-ți lista de prețuri", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Clienții care compară prețuri te găsesc și te sună direct.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { vm.query.value = it },
            placeholder = { Text("Caută: lemne, sobă, pomi, iarbă...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { vm.query.value = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Șterge căutarea")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            JobSort.entries.forEach { option ->
                FilterChip(
                    selected = sort == option,
                    onClick = { vm.sort.value = option },
                    label = { Text(option.label) },
                )
            }
            if (user.isPriceput) {
                FilterChip(
                    selected = onlyMatching,
                    onClick = { vm.onlyMatching.value = !onlyMatching },
                    label = { Text("Potrivite pentru mine") },
                )
            }
        }

        when {
            visible == null -> LoadingBox()
            visible.isEmpty() && jobs.orEmpty().isEmpty() -> EmptyState(
                "Încă nu sunt lucrări",
                "Când cineva din ${user.city} postează o lucrare, apare aici.",
                image = if (user.actsAsPriceput) R.drawable.ill_priceput_jobs else R.drawable.ill_find_priceputi,
            )
            visible.isEmpty() -> EmptyState("Nimic găsit", "Încearcă alt cuvânt sau scoate filtrele.")
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(visible, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        onClick = { onOpenJob(job.id) },
                        highlight = when {
                            job.ownerId == user.uid -> "Lucrarea ta"
                            user.uid in job.interestedIds -> "Te-ai oferit"
                            else -> null
                        },
                    )
                }
            }
        }
    }
}
