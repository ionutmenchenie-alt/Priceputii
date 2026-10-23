package ro.priceputii.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Single place the app gets its repositories from. */
object ServiceLocator {
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    val auth by lazy { AuthRepository(FirebaseAuth.getInstance()) }
    val users by lazy { UserRepository(firestore) }
    val jobs by lazy { JobRepository(firestore) }
    val chats by lazy { ChatRepository(firestore) }
}
