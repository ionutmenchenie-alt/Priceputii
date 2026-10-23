package ro.priceputii.app.ui.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.ui.chat.ChatScreen
import ro.priceputii.app.ui.jobs.JobDetailScreen
import ro.priceputii.app.ui.jobs.PostJobScreen
import ro.priceputii.app.ui.profile.PriceputProfileScreen

@Composable
fun MainNavigation(user: UserProfile) {
    val nav = rememberNavController()
    val openJob: (String) -> Unit = { nav.navigate("job/$it") }
    val openChat: (String) -> Unit = { nav.navigate("chat/$it") }
    val back: () -> Unit = { nav.popBackStack() }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                user = user,
                onOpenJob = openJob,
                onOpenChat = openChat,
                onPostJob = { nav.navigate("post") },
                onEditPriceputProfile = { nav.navigate("priceputProfile") },
            )
        }
        composable("post") {
            PostJobScreen(
                user = user,
                onBack = back,
                onPosted = { jobId ->
                    nav.navigate("job/$jobId") { popUpTo("home") }
                },
            )
        }
        composable("job/{jobId}", arguments = listOf(navArgument("jobId") { type = NavType.StringType })) {
            JobDetailScreen(user = user, onBack = back, onOpenChat = openChat)
        }
        composable("chat/{chatId}", arguments = listOf(navArgument("chatId") { type = NavType.StringType })) {
            ChatScreen(user = user, onBack = back)
        }
        composable("priceputProfile") {
            PriceputProfileScreen(user = user, onboarding = false, onDone = back, onBack = back)
        }
    }
}
