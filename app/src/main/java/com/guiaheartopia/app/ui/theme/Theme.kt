package com.guiaheartopia.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.R

val Ink = Color(0xFF4A3B2A)
val InkSubtle = Color(0xFF5C4A36)
val Muted = Color(0xFF6E5B48)
val Cream = Color(0xFFF6F0E1)
val Paper = Color(0xFFFFFBF4)
val PaperSubtle = Color(0xFFF3E7D2)
val Line = Color(0xFFD9CDB8)

val Green = Color(0xFF3E6F6A)
val GreenDark = Color(0xFF2C554F)
val GreenSoft = Color(0xFFD7E6E3)
val GreenTint = Color(0xFFB7D0CC)
val Mint = Color(0xFFE4F0EE)
val Ochre = Color(0xFFC4A15A)
val Moss = Color(0xFF6B8F71)

// Warm Honey / Sun
val Yellow = Color(0xFFF39C12)           // Golden amber
val Honey = Color(0xFFF5A623)            // Warm honey gold
val HoneySoft = Color(0xFFFFF4DE)        // Soft honey container
val HoneyTint = Color(0xFFFFE7B3)

// Coral / Sunset Peach
val Orange = Color(0xFFE76235)           // Warm coral orange
val Peach = Color(0xFFFFECE3)            // Soft peach container
val Coral = Color(0xFFE85D42)

// Sky & Waters
val Sky = Color(0xFFE4F3FA)              // Soft sky container
val SkyBlue = Color(0xFF328CD6)          // Clear bright water/sky
val SkyTint = Color(0xFFCBE7F8)

// Mystery & Ruins
val Lilac = Color(0xFFF1EBFA)            // Fairy lilac container
val Purple = Color(0xFF7E57C2)           // Ruins violet
val PurpleTint = Color(0xFFDECFF3)

// Berry & Blossom
val PinkSoft = Color(0xFFFFEEF3)         // Blossom pink
val Rose = Color(0xFFE04F75)             // Warm berry rose

val Beige = Color(0xFFF0E8D9)
val Dark = Color(0xFF2A231F)             // Warm dark coffee
val DarkCard = Color(0xFF332B26)         // Rich dark espresso for hero cards

private fun jakarta(weight: Int) = Font(
    R.font.plus_jakarta_sans,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private val Jakarta = FontFamily(
    jakarta(400),
    jakarta(500),
    jakarta(600),
    jakarta(700),
    jakarta(800),
)

private fun caveat(weight: Int) = Font(
    R.font.caveat,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private val Hand = FontFamily(
    caveat(500),
    caveat(700),
)

private val NightPaper = Color(0xFF2C2824)
private val NightInk = Color(0xFFF3E6D0)
private val NightMuted = Color(0xFFD2C3AE)
private val NightLine = Color(0xFF4A4036)
private val NightTeal = Color(0xFF8FBFB8)

private val Colors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = GreenDark,
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = Peach,
    onSecondaryContainer = Orange,
    tertiary = Honey,
    onTertiary = Ink,
    tertiaryContainer = HoneySoft,
    onTertiaryContainer = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperSubtle,
    onSurfaceVariant = Muted,
    outline = Line,
)

private fun type(ink: Color, muted: Color) = Typography(
    headlineLarge = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 44.sp, color = ink),
    headlineMedium = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 38.sp, color = ink),
    headlineSmall = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp, color = ink),
    titleLarge = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 32.sp, color = ink),
    titleMedium = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 28.sp, color = ink),
    titleSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, color = ink),
    bodyLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, color = ink),
    bodyMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, color = ink),
    bodySmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, color = muted),
    labelLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, color = ink),
    labelMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, color = ink),
    labelSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, color = muted),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun GuiaTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) {
        Colors.copy(
            primary = NightTeal,
            onPrimary = NightPaper,
            primaryContainer = Color(0xFF3A4A46),
            onPrimaryContainer = NightInk,
            background = NightPaper,
            onBackground = NightInk,
            surface = Color(0xFF342F2A),
            onSurface = NightInk,
            surfaceVariant = Color(0xFF3C362F),
            onSurfaceVariant = NightMuted,
            outline = NightLine,
        )
    } else {
        Colors
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = type(if (dark) NightInk else Ink, if (dark) NightMuted else Muted),
        shapes = AppShapes,
        content = content,
    )
}
