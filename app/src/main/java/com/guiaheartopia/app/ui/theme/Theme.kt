package com.guiaheartopia.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.R

val Ink = Color(0xFF3A271C)
val Cream = Color(0xFFFFF3E0)
val Paper = Color(0xFFFFFBF4)
val Coral = Color(0xFFE07A5F)
val Peach = Color(0xFFFFD7C2)
val Sage = Color(0xFF7EAA8A)
val Honey = Color(0xFFF2C14E)
val Sky = Color(0xFF8ECAE6)
val Lilac = Color(0xFFCDB4DB)

private val PixelFamily = FontFamily(Font(R.font.pixelify_sans, FontWeight.Normal))
private val BodyFamily = FontFamily(Font(R.font.nunito, FontWeight.Normal))

private val Colors = lightColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    secondary = Sage,
    onSecondary = Ink,
    tertiary = Honey,
    onTertiary = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Peach,
    onSurfaceVariant = Ink,
    outline = Ink,
)

private val Type = Typography(
    headlineMedium = TextStyle(fontFamily = PixelFamily, fontSize = 26.sp, lineHeight = 32.sp, color = Ink),
    headlineSmall = TextStyle(fontFamily = PixelFamily, fontSize = 20.sp, lineHeight = 26.sp, color = Ink),
    titleLarge = TextStyle(fontFamily = PixelFamily, fontSize = 18.sp, lineHeight = 24.sp, color = Ink),
    titleMedium = TextStyle(fontFamily = PixelFamily, fontSize = 15.sp, lineHeight = 20.sp, color = Ink),
    titleSmall = TextStyle(fontFamily = PixelFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Ink),
    bodyLarge = TextStyle(fontFamily = BodyFamily, fontSize = 16.sp, lineHeight = 22.sp, color = Ink),
    bodyMedium = TextStyle(fontFamily = BodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Ink),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontSize = 12.sp, lineHeight = 16.sp, color = Ink),
    labelLarge = TextStyle(fontFamily = PixelFamily, fontSize = 12.sp, lineHeight = 16.sp, color = Ink),
    labelMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink),
)

@Composable
fun GuiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, content = content)
}
