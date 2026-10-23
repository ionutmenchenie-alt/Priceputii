package ro.priceputii.app

import org.junit.Assert.assertEquals
import org.junit.Test
import ro.priceputii.app.data.Job
import ro.priceputii.app.data.JobSort
import ro.priceputii.app.data.UserProfile
import ro.priceputii.app.data.filterJobs

class JobFilterTest {
    private val jobs = listOf(
        Job(id = "1", task = "curățat soba", category = "Lemne și încălzire", zone = "Centru", price = 80),
        Job(id = "2", task = "tăiat pomi", category = "Pomi, vie și grădinărit", zone = "Ursați", price = 150),
        Job(id = "3", task = "tuns iarba", category = "Grădină și curte", zone = "Centru", price = 100, description = "curte mare"),
    )
    private val priceput = UserProfile(uid = "p", skills = listOf("tăiat pomi", "tuns iarba"), allCity = false, zones = listOf("Centru"))

    @Test
    fun keywordSearchIgnoresDiacritics() {
        assertEquals(listOf("1"), filterJobs(jobs, "soba", JobSort.NEWEST, false, priceput).map { it.id })
        assertEquals(listOf("3"), filterJobs(jobs, "curte", JobSort.NEWEST, false, priceput).map { it.id })
    }

    @Test
    fun sortsByPriceDescending() {
        assertEquals(listOf("2", "3", "1"), filterJobs(jobs, "", JobSort.PRICE_HIGH, false, priceput).map { it.id })
    }

    @Test
    fun onlyMatchingUsesSkillsAndZones() {
        assertEquals(listOf("3"), filterJobs(jobs, "", JobSort.NEWEST, true, priceput).map { it.id })
        val wholeCity = priceput.copy(allCity = true)
        assertEquals(setOf("2", "3"), filterJobs(jobs, "", JobSort.NEWEST, true, wholeCity).map { it.id }.toSet())
    }
}
