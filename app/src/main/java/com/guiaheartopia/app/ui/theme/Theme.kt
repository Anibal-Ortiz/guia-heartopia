package com.guiaheartopia.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
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

// Cozy Heartopia Color Palette
val Ink = Color(0xFF292524)              // Deep warm espresso
val InkSubtle = Color(0xFF4A4440)        // Secondary text
val Muted = Color(0xFF7E766E)            // Muted placeholder/captions
val Cream = Color(0xFFFAF7F0)            // Cozy warm milk-tea background
val Paper = Color(0xFFFFFFFF)            // Pure card surface
val PaperSubtle = Color(0xFFF7F3EA)      // Subtle card background
val Line = Color(0xFFEBE4D8)             // Delicate warm card borders

// Nature / Clover Greens
val Green = Color(0xFF2A8356)            // Vibrant clover green
val GreenDark = Color(0xFF1E6340)        // Deep forest green
val GreenSoft = Color(0xFFE6F6ED)        // Pastel mint container
val GreenTint = Color(0xFFCEECD9)        // Accent mint border
val Mint = Color(0xFFE6F7F0)

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

private val Type = Typography(
    headlineLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp, color = Ink),
    headlineMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 32.sp, color = Ink),
    headlineSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, color = Ink),
    titleLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, color = Ink),
    titleMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 23.sp, color = Ink),
    titleSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, color = Ink),
    bodyLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, color = Ink),
    bodyMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, color = Ink),
    bodySmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, color = Muted),
    labelLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, color = Ink),
    labelMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, color = Ink),
    labelSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, color = Muted),
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
    MaterialTheme(colorScheme = Colors, typography = Type, shapes = AppShapes, content = content)
}
