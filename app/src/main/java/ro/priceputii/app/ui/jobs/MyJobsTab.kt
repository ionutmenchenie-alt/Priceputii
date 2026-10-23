package ro.priceputii.app.ui.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import ro.priceputii.app.R
import ro.priceputii.app.data.Job
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.ui.components.EmptyState
import ro.priceputii.app.ui.components.LoadingBox

class MyJobsViewModel : ViewModel() {
    private val uid = ServiceLocator.auth.currentUid

    val posted: StateFlow<List<Job>?> =
        (uid?.let(ServiceLocator.jobs::observePostedBy) ?: emptyFlow())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val interested: StateFlow<List<Job>?> =
        (uid?.let(ServiceLocator.jobs::observeInterestedBy) ?: emptyFlow())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun MyJobsTab(
    user: UserProfile,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
    vm: MyJobsViewModel = viewModel(),
) {
    val posted by vm.posted.collectAsStateWithLifecycle()
    val interested by vm.interested.collectAsStateWithLifecycle()

    if (posted == null && interested == null) {
        LoadingBox(modifier)
        return
    }
    val postedJobs = posted.orEmpty()
    val interestedJobs = interested.orEmpty()
    if (postedJobs.isEmpty() && interestedJobs.isEmpty()) {
        EmptyState(
            "Nicio lucrare încă",
            if (user.actsAsPriceput) {
                "Lucrările la care te oferi apar aici. Le găsești în tabul Lucrări."
            } else {
                "Apasă „Postează lucrare” și spune ce ai de făcut."
            },
            modifier,
            image = if (user.actsAsPriceput) R.drawable.ill_priceput_jobs else R.drawable.ill_post_job,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (postedJobs.isNotEmpty()) {
            item { SectionTitle("Postate de mine") }
            items(postedJobs, key = { "p" + it.id }) { job ->
                JobCard(
                    job = job,
                    onClick = { onOpenJob(job.id) },
                    highlight = if (job.interestedIds.isEmpty()) "Așteaptă Priceputi" else null,
                )
            }
        }
        if (interestedJobs.isNotEmpty()) {
            item { SectionTitle("M-am oferit") }
            items(interestedJobs, key = { "i" + it.id }) { job ->
                JobCard(job = job, onClick = { onOpenJob(job.id) }, highlight = "Te-ai oferit")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}
