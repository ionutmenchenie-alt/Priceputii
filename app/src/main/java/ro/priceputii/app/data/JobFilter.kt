package ro.priceputii.app.data

enum class JobSort(val label: String) {
    NEWEST("Cele mai noi"),
    PRICE_HIGH("Suma cea mai mare"),
}

/** Keyword search ("lemne", "soba", "pomi"...), optional match to the Priceput's profile, then sort. */
fun filterJobs(
    jobs: List<Job>,
    query: String,
    sort: JobSort,
    onlyMatching: Boolean,
    user: UserProfile,
): List<Job> {
    val words = query.searchKey().split(' ').filter { it.isNotBlank() }
    val filtered = jobs.filter { job ->
        val haystack = "${job.task} ${job.category} ${job.description} ${job.zone}".searchKey()
        words.all { it in haystack } && (!onlyMatching || user.matches(job))
    }
    return when (sort) {
        JobSort.NEWEST -> filtered.sortedByDescending { it.createdAt }
        JobSort.PRICE_HIGH -> filtered.sortedWith(compareByDescending<Job> { it.price }.thenByDescending { it.createdAt })
    }
}
