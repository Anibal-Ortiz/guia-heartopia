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

val Ink = Color(0xFF2C2A28)
val Muted = Color(0xFF7C766F)
val Cream = Color(0xFFF6F1E6)
val Paper = Color(0xFFFFFCF8)
val Green = Color(0xFF1F7A45)
val GreenSoft = Color(0xFFE5F6EC)
val Mint = Color(0xFFE7F6EE)
val Beige = Color(0xFFEFE6D4)
val Orange = Color(0xFFC4531E)
val Yellow = Color(0xFFF0C014)
val Dark = Color(0xFF241C16)
val Line = Color(0xFFE6DFD2)
val Peach = Color(0xFFFFE8D6)
val Honey = Yellow
val Sage = Green
val Coral = Orange
val Sky = Color(0xFFD7EFE4)
val Lilac = Color(0xFFE8DDF2)

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
    onPrimaryContainer = Green,
    secondary = Orange,
    onSecondary = Color.White,
    tertiary = Yellow,
    onTertiary = Dark,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Beige,
    onSurfaceVariant = Muted,
    outline = Line,
)

private val Type = Typography(
    headlineMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp, color = Ink),
    headlineSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, color = Ink),
    titleLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, color = Ink),
    titleMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp, color = Ink),
    titleSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, color = Ink),
    bodyLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, color = Ink),
    bodyMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, color = Ink),
    bodySmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, color = Muted),
    labelLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, color = Ink),
    labelMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, color = Ink),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun GuiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, shapes = AppShapes, content = content)
}
