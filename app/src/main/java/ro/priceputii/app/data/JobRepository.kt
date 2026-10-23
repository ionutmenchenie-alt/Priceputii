package ro.priceputii.app.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class JobRepository(private val db: FirebaseFirestore) {

    private val jobs = db.collection("jobs")

    private fun DocumentSnapshot.toJob(): Job? = toObject(Job::class.java)?.copy(
        id = id,
        // Our own just-posted job has no server time yet; use the local estimate so it sorts first.
        createdAt = getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
    )

    /** Newest open jobs. Filtering and sorting happen on the phone, so no Firestore indexes are needed. */
    fun observeOpenJobs(): Flow<List<Job>> =
        jobs.orderBy("createdAt", Query.Direction.DESCENDING).limit(300).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toJob() }.filter { it.status == JobStatus.OPEN }
        }

    fun observeJob(jobId: String): Flow<Job?> =
        jobs.document(jobId).snapshots().map { if (it.exists()) it.toJob() else null }

    fun observePostedBy(uid: String): Flow<List<Job>> =
        jobs.whereEqualTo("ownerId", uid).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toJob() }.sortedByDescending { it.createdAt }
        }

    fun observeInterestedBy(uid: String): Flow<List<Job>> =
        jobs.whereArrayContains("interestedIds", uid).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toJob() }.sortedByDescending { it.createdAt }
        }

    fun observeInterests(jobId: String): Flow<List<Interest>> =
        jobs.document(jobId).collection("interests").snapshots().map { snap ->
            snap.documents.mapNotNull { it.toObject(Interest::class.java) }.sortedBy { it.createdAt }
        }

    suspend fun getContact(jobId: String): JobContact? =
        jobs.document(jobId).collection("private").document("contact").get().await()
            .toObject(JobContact::class.java)

    suspend fun post(
        owner: UserProfile,
        ownerPhone: String,
        task: String,
        zone: String,
        address: String,
        description: String,
        whenText: String,
        fromPlace: String,
        toPlace: String,
        price: Long,
    ): String {
        val doc = jobs.document()
        db.batch()
            .set(
                doc,
                mapOf(
                    "ownerId" to owner.uid,
                    "ownerName" to owner.displayName,
                    "category" to Catalog.categoryOf(task).orEmpty(),
                    "task" to task,
                    "description" to description,
                    "city" to owner.city,
                    "zone" to zone,
                    "whenText" to whenText,
                    "fromPlace" to fromPlace,
                    "toPlace" to toPlace,
                    "price" to price,
                    "status" to JobStatus.OPEN,
                    "interestedIds" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )
            .set(
                doc.collection("private").document("contact"),
                mapOf("phone" to ownerPhone, "address" to address),
            )
            .commit()
            .await()
        return doc.id
    }

    /** The quick "Sunt interesat de lucrarea dvs." button: hands the client this Priceput's name and phone. */
    suspend fun expressInterest(jobId: String, me: UserProfile, myPhone: String) {
        val job = jobs.document(jobId)
        db.batch()
            .set(
                job.collection("interests").document(me.uid),
                mapOf(
                    "priceputId" to me.uid,
                    "name" to me.displayName,
                    "phone" to myPhone,
                    "starsTotal" to me.starsTotal,
                    "ratingCount" to me.ratingCount,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )
            .update(job, "interestedIds", FieldValue.arrayUnion(me.uid))
            .commit()
            .await()
    }

    suspend fun markDone(jobId: String) {
        jobs.document(jobId).update("status", JobStatus.DONE).await()
    }
}
