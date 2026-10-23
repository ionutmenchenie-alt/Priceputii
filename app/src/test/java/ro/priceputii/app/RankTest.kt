package ro.priceputii.app

import org.junit.Assert.assertEquals
import org.junit.Test
import ro.priceputii.app.data.Rank
import ro.priceputii.app.data.rankFor
import ro.priceputii.app.data.rankSummary

class RankTest {
    @Test
    fun newPriceputStartsAsPriceput() {
        assertEquals(Rank.PRICEPUT, rankFor(0, 0))
        assertEquals(Rank.PRICEPUT, rankFor(45, 9))
    }

    @Test
    fun ranksFollowStarsAndAverage() {
        assertEquals(Rank.MESTER, rankFor(50, 10))
        assertEquals(Rank.MAISTRU, rankFor(250, 50))
    }

    @Test
    fun lowAverageDropsARank() {
        assertEquals(Rank.MESTER, rankFor(300, 70)) // average 4.28 < 4.5
        assertEquals(Rank.PRICEPUT, rankFor(60, 20)) // average 3.0 < 4.0
    }

    @Test
    fun summaryReadsNaturally() {
        assertEquals("Priceput", rankSummary(0, 0))
        assertEquals("Meșter · 4,8 stele · 38 lucrări", rankSummary(182, 38))
    }
}
