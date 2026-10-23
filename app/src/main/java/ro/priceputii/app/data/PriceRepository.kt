package ro.priceputii.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/** Price lists: one document per Priceput and task, so clients can compare prices for a task. */
class PriceRepository(private val db: FirebaseFirestore) {

    private val prices = db.collection("prices")

    fun idFor(uid: String, task: String): String =
        uid + "_" + task.searchKey().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    fun observeMine(uid: String): Flow<List<PriceEntry>> =
        prices.whereEqualTo("priceputId", uid).snapshots().map { snap ->
            snap.documents.mapNotNull { doc -> doc.toObject(PriceEntry::class.java)?.copy(id = doc.id) }
        }

    /** Everyone's price for a task in the city, cheapest first. */
    fun observeForTask(task: String, city: String): Flow<List<PriceEntry>> =
        prices.whereEqualTo("task", task).snapshots().map { snap ->
            snap.documents.mapNotNull { doc -> doc.toObject(PriceEntry::class.java)?.copy(id = doc.id) }
                .filter { it.city == city && it.price > 0 }
                .sortedBy { it.price }
        }

    /** Replaces the user's list with [entries] (task to price and unit). */
    suspend fun saveMine(user: UserProfile, phone: String, entries: Map<String, Pair<Long, String>>, previous: List<PriceEntry>) {
        val batch = db.batch()
        val keep = entries.keys.map { idFor(user.uid, it) }.toSet()
        previous.filter { it.id !in keep }.forEach { batch.delete(prices.document(it.id)) }
        entries.forEach { (task, value) ->
            batch.set(
                prices.document(idFor(user.uid, task)),
                mapOf(
                    "priceputId" to user.uid,
                    "name" to user.displayName,
                    "avatar" to user.avatar,
                    "phone" to phone,
                    "city" to user.city,
                    "task" to task,
                    "price" to value.first,
                    "unit" to value.second,
                    "starsTotal" to user.starsTotal,
                    "ratingCount" to user.ratingCount,
                ),
            )
        }
        batch.update(db.collection("users").document(user.uid), "hasPriceList", entries.isNotEmpty())
        batch.commit().await()
    }
}
