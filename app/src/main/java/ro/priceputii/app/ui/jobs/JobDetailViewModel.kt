package ro.priceputii.app.ui.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ro.priceputii.app.data.Interest
import ro.priceputii.app.data.Job
import ro.priceputii.app.data.JobContact
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile

@OptIn(ExperimentalCoroutinesApi::class)
class JobDetailViewModel(handle: SavedStateHandle) : ViewModel() {

    private val jobId: String = checkNotNull(handle["jobId"])
    private val me: String = ServiceLocator.auth.currentUid.orEmpty()
    private val jobs = ServiceLocator.jobs

    /** null while loading; a Job with an empty id when it was deleted. */
    val job: StateFlow<Job?> = jobs.observeJob(jobId)
        .map { it ?: Job() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val access = job.map { j ->
        when {
            j == null || j.id.isEmpty() -> Access.NONE
            j.ownerId == me -> Access.OWNER
            me in j.interestedIds -> Access.INTERESTED
            else -> Access.NONE
        }
    }.distinctUntilChanged()

    /** Priceputi who pressed "Sunt interesat"; only the owner can see them. */
    val interests: StateFlow<List<Interest>> = access
        .flatMapLatest { if (it == Access.OWNER) jobs.observeInterests(jobId) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Exact address and phone, unlocked for the owner and for interested Priceputi. */
    val contact: StateFlow<JobContact?> = access
        .flatMapLatest { level ->
            if (level == Access.NONE) flowOf(null) else flow { emit(loadContact()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Right after "Sunt interesat" the server may not have the interest yet, so retry a little. */
    private suspend fun loadContact(): JobContact? {
        repeat(4) { attempt ->
            runCatching { jobs.getContact(jobId) }.getOrNull()?.let { return it }
            delay(1_000L * (attempt + 1))
        }
        return null
    }

    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun expressInterest(user: UserProfile) = launchAction("Nu am putut trimite interesul") {
        jobs.expressInterest(jobId, user, ServiceLocator.auth.currentPhone)
    }

    fun markDone() = launchAction("Nu am putut marca lucrarea") { jobs.markDone(jobId) }

    fun openChat(user: UserProfile, otherId: String, otherName: String, jobTitle: String, onOpened: (String) -> Unit) =
        launchAction("Nu am putut deschide conversația") {
            onOpened(ServiceLocator.chats.openChat(user.uid, user.displayName, otherId, otherName, jobTitle))
        }

    private fun launchAction(failure: String, block: suspend () -> Unit) {
        if (busy) return
        viewModelScope.launch {
            busy = true
            error = null
            runCatching { block() }.onFailure { error = "$failure: ${it.localizedMessage}" }
            busy = false
        }
    }

    private enum class Access { NONE, OWNER, INTERESTED }
}
