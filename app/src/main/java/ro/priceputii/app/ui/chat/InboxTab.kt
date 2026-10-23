package ro.priceputii.app.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import ro.priceputii.app.R
import ro.priceputii.app.data.Chat
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.ui.components.Avatar
import ro.priceputii.app.ui.components.EmptyState
import ro.priceputii.app.ui.components.LoadingBox
import ro.priceputii.app.ui.components.timeAgo

class InboxViewModel : ViewModel() {
    val chats: StateFlow<List<Chat>?> =
        (ServiceLocator.auth.currentUid?.let(ServiceLocator.chats::observeChats) ?: emptyFlow())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun InboxTab(user: UserProfile, chats: List<Chat>?, onOpenChat: (String) -> Unit, modifier: Modifier = Modifier) {
    when {
        chats == null -> LoadingBox(modifier)
        chats.isEmpty() -> EmptyState(
            "Niciun mesaj încă",
            "Conversațiile cu clienții și Priceputii apar aici.",
            modifier,
            image = R.drawable.ill_messages,
        )
        else -> LazyColumn(modifier.fillMaxSize()) {
            item {
                Text(
                    "Mesaje",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp),
                )
            }
            items(chats, key = { it.id }) { chat ->
                ChatRow(chat, user.uid, onClick = { onOpenChat(chat.id) })
                HorizontalDivider(Modifier.padding(start = 72.dp))
            }
        }
    }
}

@Composable
private fun ChatRow(chat: Chat, me: String, onClick: () -> Unit) {
    val unread = chat.isUnread(me)
    val name = chat.otherName(me)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Avatar(chat.otherAvatar(me), name)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(
                    timeAgo(chat.lastAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (chat.jobTitle.isNotBlank()) {
                Text(
                    chat.jobTitle.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (chat.lastSenderId == me) "Tu: " else "") + chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (unread) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(10.dp)) {}
                }
            }
        }
    }
}
