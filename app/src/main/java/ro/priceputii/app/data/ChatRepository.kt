package ro.priceputii.app.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ChatRepository(private val db: FirebaseFirestore) {

    private val chats = db.collection("chats")

    /** One conversation per pair of people, whatever job it started from. */
    fun chatIdFor(a: String, b: String): String = listOf(a, b).sorted().joinToString("_")

    suspend fun openChat(
        me: UserProfile,
        other: String,
        otherName: String,
        otherAvatar: String,
        jobTitle: String,
    ): String {
        val id = chatIdFor(me.uid, other)
        chats.document(id).set(
            mapOf(
                "participants" to listOf(me.uid, other).sorted(),
                "names" to mapOf(me.uid to me.displayName, other to otherName),
                "avatars" to mapOf(me.uid to me.avatar, other to otherAvatar),
                "jobTitle" to jobTitle,
            ),
            SetOptions.merge(),
        ).await()
        return id
    }

    fun observeChats(uid: String): Flow<List<Chat>> =
        chats.whereArrayContains("participants", uid).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toChat() }
                .filter { it.lastMessage.isNotEmpty() }
                .sortedByDescending { it.lastAt ?: Timestamp(0, 0) }
        }

    fun observeChat(chatId: String): Flow<Chat?> =
        chats.document(chatId).snapshots().map { if (it.exists()) it.toChat() else null }

    fun observeMessages(chatId: String): Flow<List<Message>> =
        chats.document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limitToLast(500)
            .snapshots()
            .map { snap ->
                snap.documents.map { doc ->
                    Message(
                        id = doc.id,
                        senderId = doc.getString("senderId").orEmpty(),
                        text = doc.getString("text").orEmpty(),
                        createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                    )
                }
            }

    suspend fun send(chatId: String, me: String, text: String) {
        val chat = chats.document(chatId)
        db.batch()
            .set(
                chat.collection("messages").document(),
                mapOf("senderId" to me, "text" to text, "createdAt" to FieldValue.serverTimestamp()),
            )
            .update(
                chat,
                mapOf(
                    "lastMessage" to text,
                    "lastSenderId" to me,
                    "lastAt" to FieldValue.serverTimestamp(),
                    "lastRead.$me" to FieldValue.serverTimestamp(),
                ),
            )
            .commit()
            .await()
    }

    suspend fun markRead(chatId: String, me: String) {
        chats.document(chatId).update("lastRead.$me", FieldValue.serverTimestamp()).await()
    }

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toChat(): Chat? {
        val participants = get("participants") as? List<String> ?: return null
        val readRaw = get("lastRead", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) as? Map<String, Any?>
        return Chat(
            id = id,
            participants = participants,
            names = (get("names") as? Map<String, String>).orEmpty(),
            avatars = (get("avatars") as? Map<String, String>).orEmpty(),
            jobTitle = getString("jobTitle").orEmpty(),
            lastMessage = getString("lastMessage").orEmpty(),
            lastSenderId = getString("lastSenderId").orEmpty(),
            lastAt = getTimestamp("lastAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
            lastRead = readRaw.orEmpty().mapNotNull { (k, v) -> (v as? Timestamp)?.let { k to it } }.toMap(),
        )
    }
}
