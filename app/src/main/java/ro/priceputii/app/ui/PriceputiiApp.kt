package ro.priceputii.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ro.priceputii.app.data.Roles
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.ui.onboarding.SplashScreen
import ro.priceputii.app.ui.home.MainNavigation
import ro.priceputii.app.ui.onboarding.OnboardingNavigation
import ro.priceputii.app.ui.profile.PriceputProfileScreen

@Composable
fun PriceputiiApp(session: SessionViewModel = viewModel()) {
    val state by session.state.collectAsStateWithLifecycle()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val current = state) {
            SessionState.Loading -> SplashScreen()
            SessionState.SignedOut, is SessionState.NoProfile -> OnboardingNavigation()
            is SessionState.Ready -> {
                val user = current.user
                if (user.actsAsPriceput && user.skills.isEmpty()) {
                    // A new Priceput first says where they work and what they know how to do.
                    val scope = rememberCoroutineScope()
                    PriceputProfileScreen(
                        user = user,
                        onboarding = true,
                        onDone = {},
                        // Someone who also posts jobs can step back to that role instead.
                        onBack = if (Roles.CLIENT in user.roles) {
                            { scope.launch { runCatching { ServiceLocator.users.setActiveRole(user.uid, Roles.CLIENT) } } }
                        } else {
                            null
                        },
                    )
                } else {
                    MainNavigation(user)
                }
            }
        }
    }
}
