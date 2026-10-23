package ro.priceputii.app.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun OnboardingNavigation() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "role") {
        composable("role") {
            RoleScreen(onRoleChosen = { role -> nav.navigate("login/$role") })
        }
        composable(
            "login/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType }),
        ) {
            LoginScreen(onBack = { nav.popBackStack() })
        }
    }
}
