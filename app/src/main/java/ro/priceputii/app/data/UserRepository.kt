package ro.priceputii.app.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    private fun ref(uid: String) = db.collection("users").document(uid)

    /** The user's profile, or null while they haven't registered yet. */
    fun observe(uid: String): Flow<UserProfile?> = ref(uid).snapshots().map { snap ->
        if (snap.exists()) snap.toObject(UserProfile::class.java)?.copy(uid = uid) else null
    }

    /** Creates the profile on first sign-in, or adds the role to an existing one. */
    suspend fun register(uid: String, firstName: String, lastName: String, role: String) {
        val doc = ref(uid)
        db.runTransaction { tx ->
            val existing = tx.get(doc)
            if (existing.exists()) {
                tx.update(
                    doc,
                    mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "roles" to FieldValue.arrayUnion(role),
                        "activeRole" to role,
                    ),
                )
            } else {
                tx.set(
                    doc,
                    mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "roles" to listOf(role),
                        "activeRole" to role,
                        "city" to Catalog.CITY,
                        "allCity" to true,
                        "zones" to emptyList<String>(),
                        "radiusKm" to 10,
                        "skills" to emptyList<String>(),
                        "starsTotal" to 0,
                        "ratingCount" to 0,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
            }
        }.await()
    }

    suspend fun savePriceputProfile(
        uid: String,
        allCity: Boolean,
        zones: List<String>,
        radiusKm: Int,
        skills: List<String>,
    ) {
        ref(uid).update(
            mapOf(
                "allCity" to allCity,
                "zones" to zones,
                "radiusKm" to radiusKm,
                "skills" to skills,
                "roles" to FieldValue.arrayUnion(Roles.PRICEPUT),
                "activeRole" to Roles.PRICEPUT,
            ),
        ).await()
    }

    suspend fun setActiveRole(uid: String, role: String) {
        ref(uid).update(
            mapOf(
                "activeRole" to role,
                "roles" to FieldValue.arrayUnion(role),
            ),
        ).await()
    }
}
