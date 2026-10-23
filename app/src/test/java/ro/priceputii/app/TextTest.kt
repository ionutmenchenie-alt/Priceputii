package ro.priceputii.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ro.priceputii.app.data.normalizeRoPhone
import ro.priceputii.app.data.searchKey

class TextTest {
    @Test
    fun phoneNumbersAreNormalizedToE164() {
        assertEquals("+40722123456", normalizeRoPhone("0722 123 456"))
        assertEquals("+40722123456", normalizeRoPhone("40722123456"))
        assertEquals("+40722123456", normalizeRoPhone("+40 722-123-456"))
        assertEquals("+40722123456", normalizeRoPhone("0040722123456"))
        assertEquals("+40700000001", normalizeRoPhone("0700 000 001"))
    }

    @Test
    fun invalidPhoneNumbersAreRejected() {
        assertNull(normalizeRoPhone("123"))
        assertNull(normalizeRoPhone("0253 123 456"))
        assertNull(normalizeRoPhone(""))
    }

    @Test
    fun searchIgnoresDiacriticsAndCase() {
        assertEquals("curatat soba", "Curățat Sobă".searchKey())
        assertEquals("taiat lemne", "TĂIAT lemne".searchKey())
    }
}
