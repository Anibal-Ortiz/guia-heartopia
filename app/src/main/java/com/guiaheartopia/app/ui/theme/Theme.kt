package com.guiaheartopia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
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

// Paleta semántica (cozy · tonos cálidos)
val Ink = Color(0xFF4A3F35)
val Muted = Color(0xFF8A7B6E)
val Cream = Color(0xFFF7F2E8)
val Paper = Color(0xFFFFFBF5)
val Green = Color(0xFF5B9A6F)
val GreenSoft = Color(0xFFDFF0E4)
val Mint = Color(0xFFE8F5EC)
val Beige = Color(0xFFF0E6D6)
val Orange = Color(0xFFD4845C)
val Yellow = Color(0xFFF2C94C)
val Dark = Color(0xFF3D3229)
val Line = Color(0xFFE8E0D4)
val Peach = Color(0xFFFFE8D6)
val Honey = Yellow
val Sage = Green
val Coral = Color(0xFFE8A07A)
val Sky = Color(0xFFDCEFE6)
val Lilac = Color(0xFFEDE4F4)

private val Pixelify = FontFamily(
    Font(R.font.pixelify_sans, FontWeight.Normal),
    Font(R.font.pixelify_sans, FontWeight.Medium),
    Font(R.font.pixelify_sans, FontWeight.SemiBold),
    Font(R.font.pixelify_sans, FontWeight.Bold),
)

private val Nunito = FontFamily(
    Font(R.font.nunito, FontWeight.Normal),
    Font(R.font.nunito, FontWeight.Medium),
    Font(R.font.nunito, FontWeight.SemiBold),
    Font(R.font.nunito, FontWeight.Bold),
)

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
)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = Color(0xFF2D5A3C),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Peach,
    onSecondaryContainer = Color(0xFF6B4030),
    tertiary = Yellow,
    onTertiary = Dark,
    tertiaryContainer = Color(0xFFFFF3C4),
    onTertiaryContainer = Color(0xFF5C4A1E),
    error = Color(0xFFBA5548),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF5C231C),
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Beige,
    onSurfaceVariant = Muted,
    outline = Line,
    outlineVariant = Color(0xFFD9D0C4),
    scrim = Color(0x664A3F35),
    inverseSurface = Color(0xFF3A332C),
    inverseOnSurface = Color(0xFFF5EDE3),
    inversePrimary = Color(0xFF8BC4A0),
    surfaceTint = Green,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BC4A0),
    onPrimary = Color(0xFF1E2E24),
    primaryContainer = Color(0xFF3D5C48),
    onPrimaryContainer = Color(0xFFDFF0E4),
    secondary = Color(0xFFE8B89A),
    onSecondary = Color(0xFF4A3224),
    secondaryContainer = Color(0xFF6B4A38),
    onSecondaryContainer = Color(0xFFFFE8D6),
    tertiary = Color(0xFFF5D76E),
    onTertiary = Color(0xFF3D3220),
    tertiaryContainer = Color(0xFF5C4F2A),
    onTertiaryContainer = Color(0xFFFFF3C4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5C1510),
    errorContainer = Color(0xFF7D2E26),
    onErrorContainer = Color(0xFFFFDAD4),
    background = Color(0xFF2C2620),
    onBackground = Color(0xFFF5EDE3),
    surface = Color(0xFF3A332C),
    onSurface = Color(0xFFF5EDE3),
    surfaceVariant = Color(0xFF4A4239),
    onSurfaceVariant = Color(0xFFC4B8AB),
    outline = Color(0xFF8F8275),
    outlineVariant = Color(0xFF5C534A),
    scrim = Color(0xCC1A1612),
    inverseSurface = Color(0xFFF5EDE3),
    inverseOnSurface = Color(0xFF3A332C),
    inversePrimary = Green,
    surfaceTint = Color(0xFF8BC4A0),
)

private val Heading = Pixelify
private val Body = Nunito
private val Label = Jakarta

private val Type = Typography(
    displayLarge = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Heading,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Label,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Label,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.25.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Label,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.25.sp,
    ),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun GuiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Type,
        shapes = AppShapes,
        content = content,
    )
}

/** Alias descriptivo del tema de la app. */
@Composable
fun GuiaHeartopiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) = GuiaTheme(darkTheme = darkTheme, content = content)
