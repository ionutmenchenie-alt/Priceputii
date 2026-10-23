package ro.priceputii.app.data

import java.text.Normalizer

private val diacritics = "\\p{M}+".toRegex()

/** Lowercase without diacritics, so "soba" finds "sobă" and "taiat" finds "tăiat". */
fun String.searchKey(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(diacritics, "")

/**
 * Turns what people type ("0722 123 456", "40722123456", "+40 722-123-456")
 * into E.164 (+40722123456). Returns null when it can't be a Romanian mobile number.
 */
fun normalizeRoPhone(input: String): String? {
    val digits = input.filter { it.isDigit() || it == '+' }
    val e164 = when {
        digits.startsWith("+") -> digits
        digits.startsWith("0040") -> "+" + digits.drop(2)
        digits.startsWith("40") -> "+$digits"
        digits.startsWith("0") -> "+40" + digits.drop(1)
        else -> return null
    }
    return e164.takeIf { it.matches(Regex("\\+407\\d{8}")) }
}
