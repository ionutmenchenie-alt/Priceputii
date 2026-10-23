package ro.priceputii.app.data

import com.google.firebase.Timestamp

object Roles {
    const val PRICEPUT = "priceput"
    const val CLIENT = "client"
}

data class UserProfile(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val roles: List<String> = emptyList(),
    val activeRole: String = Roles.CLIENT,
    val city: String = Catalog.CITY,
    val allCity: Boolean = true,
    val zones: List<String> = emptyList(),
    val radiusKm: Int = 10,
    val skills: List<String> = emptyList(),
    val starsTotal: Long = 0,
    val ratingCount: Long = 0,
) {
    val displayName: String get() = "$firstName $lastName".trim()
    val isPriceput: Boolean get() = Roles.PRICEPUT in roles
    val actsAsPriceput: Boolean get() = activeRole == Roles.PRICEPUT

    /** A Priceput is notified about (and can filter to) jobs of a task they know, in a zone they cover. */
    fun matches(job: Job): Boolean =
        job.task in skills && job.city == city && (allCity || job.zone in zones)
}

object JobStatus {
    const val OPEN = "open"
    const val DONE = "done"
}

data class Job(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val category: String = "",
    val task: String = "",
    val description: String = "",
    val city: String = Catalog.CITY,
    val zone: String = "",
    val whenText: String = "",
    val fromPlace: String = "",
    val toPlace: String = "",
    val price: Long = 0,
    val status: String = JobStatus.OPEN,
    val interestedIds: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
)

/** Exact address and phone of a job; readable only by its owner and Priceputi who said they're interested. */
data class JobContact(
    val phone: String = "",
    val address: String = "",
)

data class Interest(
    val priceputId: String = "",
    val name: String = "",
    val phone: String = "",
    val starsTotal: Long = 0,
    val ratingCount: Long = 0,
    val createdAt: Timestamp? = null,
)

data class Chat(
    val id: String,
    val participants: List<String>,
    val names: Map<String, String>,
    val jobTitle: String,
    val lastMessage: String,
    val lastSenderId: String,
    val lastAt: Timestamp?,
    val lastRead: Map<String, Timestamp>,
) {
    fun otherId(me: String): String = participants.firstOrNull { it != me } ?: me
    fun otherName(me: String): String = names[otherId(me)].orEmpty().ifBlank { "Utilizator" }
    fun isUnread(me: String): Boolean {
        if (lastSenderId.isEmpty() || lastSenderId == me) return false
        val read = lastRead[me] ?: return true
        val last = lastAt ?: return false
        return last > read
    }
}

data class Message(
    val id: String,
    val senderId: String,
    val text: String,
    val createdAt: Timestamp?,
)
