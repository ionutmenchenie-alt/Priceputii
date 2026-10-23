package ro.priceputii.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import ro.priceputii.app.R

val BrandGreen = Color(0xFF1E6B45)
val BrandGreenDark = Color(0xFF14492F)
val BrandGreenSoft = Color(0xFFE3F0E8)
val BrandOrange = Color(0xFFC2410C)
val BrandOrangeSoft = Color(0xFFFDEBDF)
val Ground = Color(0xFFFAFAF7)
val Ink = Color(0xFF1B1F1C)
val InkMuted = Color(0xFF5B635E)

private val colors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreenSoft,
    onPrimaryContainer = BrandGreenDark,
    secondary = BrandOrange,
    onSecondary = Color.White,
    secondaryContainer = BrandOrangeSoft,
    onSecondaryContainer = Color(0xFF7A2806),
    background = Ground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0F2EE),
    onSurfaceVariant = InkMuted,
    outline = Color(0xFFC9CFCA),
)

@OptIn(ExperimentalTextApi::class)
private fun manrope(weight: Int) = Font(
    R.font.manrope,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Manrope = FontFamily(manrope(400), manrope(500), manrope(600), manrope(700), manrope(800))

private val base = Typography()

private fun TextStyle.manrope() = copy(fontFamily = Manrope)

private val typography = Typography(
    displayLarge = base.displayLarge.manrope(),
    displayMedium = base.displayMedium.manrope(),
    displaySmall = base.displaySmall.manrope(),
    headlineLarge = base.headlineLarge.manrope().copy(fontWeight = FontWeight.ExtraBold),
    headlineMedium = base.headlineMedium.manrope().copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = base.headlineSmall.manrope().copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.manrope().copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.manrope().copy(fontWeight = FontWeight.Bold),
    titleSmall = base.titleSmall.manrope().copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.manrope(),
    bodyMedium = base.bodyMedium.manrope(),
    bodySmall = base.bodySmall.manrope(),
    labelLarge = base.labelLarge.manrope().copy(fontWeight = FontWeight.Bold),
    labelMedium = base.labelMedium.manrope().copy(fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.manrope(),
)

@Composable
fun PriceputiiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
