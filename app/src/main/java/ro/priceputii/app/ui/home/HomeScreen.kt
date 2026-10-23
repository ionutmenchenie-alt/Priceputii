package ro.priceputii.app.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.ui.chat.InboxTab
import ro.priceputii.app.ui.chat.InboxViewModel
import ro.priceputii.app.ui.jobs.JobListTab
import ro.priceputii.app.ui.jobs.MyJobsTab
import ro.priceputii.app.ui.profile.ProfileTab

private enum class Tab(val label: String, val icon: ImageVector) {
    JOBS("Lucrări", Icons.Filled.Search),
    MINE("Ale mele", Icons.AutoMirrored.Filled.List),
    MESSAGES("Mesaje", Icons.Filled.Email),
    PROFILE("Profil", Icons.Filled.Person),
}

@Composable
fun HomeScreen(
    user: UserProfile,
    onOpenJob: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onPostJob: () -> Unit,
    onEditPriceputProfile: () -> Unit,
    inbox: InboxViewModel = viewModel(),
) {
    var tab by rememberSaveable { mutableIntStateOf(if (user.actsAsPriceput) Tab.JOBS.ordinal else Tab.MINE.ordinal) }
    val chats by inbox.chats.collectAsStateWithLifecycle()
    val unread = chats.orEmpty().count { it.isUnread(user.uid) }
    val current = Tab.entries[tab]
    val showPost = current == Tab.MINE || (current == Tab.JOBS && !user.actsAsPriceput)

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = item == current,
                        onClick = { tab = item.ordinal },
                        label = { Text(item.label) },
                        icon = {
                            if (item == Tab.MESSAGES && unread > 0) {
                                BadgedBox(badge = { Badge { Text("$unread") } }) {
                                    Icon(item.icon, contentDescription = null)
                                }
                            } else {
                                Icon(item.icon, contentDescription = null)
                            }
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            if (showPost) {
                ExtendedFloatingActionButton(
                    onClick = onPostJob,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Postează lucrare") },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                )
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (current) {
            Tab.JOBS -> JobListTab(user = user, onOpenJob = onOpenJob, modifier = modifier)
            Tab.MINE -> MyJobsTab(user = user, onOpenJob = onOpenJob, modifier = modifier)
            Tab.MESSAGES -> InboxTab(user = user, chats = chats, onOpenChat = onOpenChat, modifier = modifier)
            Tab.PROFILE -> ProfileTab(user = user, onEditPriceputProfile = onEditPriceputProfile, modifier = modifier)
        }
    }
}
