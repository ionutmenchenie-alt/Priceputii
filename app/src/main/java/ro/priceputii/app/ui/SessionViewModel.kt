package ro.priceputii.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    /** Signed in with the phone, but the name/role step isn't done yet. */
    data class NoProfile(val uid: String) : SessionState
    data class Ready(val user: UserProfile) : SessionState
}

class SessionViewModel : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<SessionState> = ServiceLocator.auth.uid
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(SessionState.SignedOut)
            } else {
                ServiceLocator.users.observe(uid).map { user ->
                    if (user == null) SessionState.NoProfile(uid) else SessionState.Ready(user)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)
}
