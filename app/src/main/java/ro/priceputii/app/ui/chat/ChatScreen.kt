package ro.priceputii.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ro.priceputii.app.data.Chat
import ro.priceputii.app.data.Message
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.ui.components.clockTime

class ChatViewModel(handle: SavedStateHandle) : ViewModel() {
    private val chatId: String = checkNotNull(handle["chatId"])
    private val me = ServiceLocator.auth.currentUid.orEmpty()
    private val chats = ServiceLocator.chats

    val chat: StateFlow<Chat?> = chats.observeChat(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val messages: StateFlow<List<Message>> = chats.observeMessages(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var draft by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
        private set

    fun send() {
        val text = draft.trim()
        if (text.isEmpty()) return
        draft = ""
        error = null
        viewModelScope.launch {
            runCatching { chats.send(chatId, me, text.take(2000)) }.onFailure {
                draft = text
                error = "Mesajul nu a plecat: ${it.localizedMessage}"
            }
        }
    }

    fun markRead() {
        viewModelScope.launch { runCatching { chats.markRead(chatId, me) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    user: UserProfile,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel(),
) {
    val chat by vm.chat.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, chat?.lastAt) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
        if (chat?.isUnread(user.uid) == true) vm.markRead()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(chat?.otherName(user.uid).orEmpty(), style = MaterialTheme.typography.titleMedium)
                        chat?.jobTitle?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it.replaceFirstChar { c -> c.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (messages.isEmpty()) {
                    item {
                        Text(
                            "Scrie primul mesaj. Nu da bani în avans și nu trimite date de card prin chat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                items(messages, key = { it.id }) { message ->
                    Bubble(message, mine = message.senderId == user.uid)
                }
            }
            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
            ) {
                OutlinedTextField(
                    value = vm.draft,
                    onValueChange = { vm.draft = it },
                    placeholder = { Text("Scrie un mesaj") },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = vm::send, enabled = vm.draft.isNotBlank()) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Trimite",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun Bubble(message: Message, mine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (mine) 16.dp else 4.dp,
                bottomEnd = if (mine) 4.dp else 16.dp,
            ),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(message.text, style = MaterialTheme.typography.bodyLarge)
                Text(
                    clockTime(message.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}
