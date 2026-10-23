package ro.priceputii.app.data

import java.util.Locale

enum class Rank(val label: String) {
    PRICEPUT("Priceput"),
    MESTER("Meșter"),
    MAISTRU("Maistru"),
}

/** Rank from stars collected and their average; falling below an average drops the rank. */
fun rankFor(starsTotal: Long, ratingCount: Long): Rank {
    if (ratingCount <= 0) return Rank.PRICEPUT
    val average = starsTotal.toDouble() / ratingCount
    return when {
        starsTotal >= 250 && average >= 4.5 -> Rank.MAISTRU
        starsTotal >= 50 && average >= 4.0 -> Rank.MESTER
        else -> Rank.PRICEPUT
    }
}

/** "Meșter · 4,8 stele · 38 lucrări", or just the rank while there are no ratings yet. */
fun rankSummary(starsTotal: Long, ratingCount: Long): String {
    val rank = rankFor(starsTotal, ratingCount).label
    if (ratingCount <= 0) return rank
    val average = String.format(Locale("ro", "RO"), "%.1f", starsTotal.toDouble() / ratingCount)
    val jobs = if (ratingCount == 1L) "1 lucrare" else "$ratingCount lucrări"
    return "$rank · $average stele · $jobs"
}
