package com.guiaheartopia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.R

/**
 * Paleta completa de la app. Los nombres públicos (`Ink`, `Paper`, ...) siguen existiendo
 * como getters composables que leen de [LocalAppPalette], de modo que cada llamada resuelve
 * el color según el tema activo sin tocar los cientos de call sites existentes.
 *
 * [LightPalette] replica exactamente los valores originales: el tema claro no cambia.
 */
data class AppPalette(
    val ink: Color,
    val inkSubtle: Color,
    val muted: Color,
    val cream: Color,
    val paper: Color,
    val paperSubtle: Color,
    val line: Color,
    /** Texto secundario sobre el banner de cabecera (literal original en modo claro). */
    val bannerSubtitle: Color,
    val green: Color,
    val greenDark: Color,
    val greenSoft: Color,
    val greenTint: Color,
    val mint: Color,
    val ochre: Color,
    val ochreDark: Color,
    val moss: Color,
    val yellow: Color,
    val honey: Color,
    val honeySoft: Color,
    val honeyTint: Color,
    val orange: Color,
    val peach: Color,
    val coral: Color,
    val sky: Color,
    val skyBlue: Color,
    val skyTint: Color,
    val lilac: Color,
    val purple: Color,
    val purpleTint: Color,
    val pinkSoft: Color,
    val rose: Color,
    val beige: Color,
    val dark: Color,
    val darkCard: Color,
    val cardPaper: Color,
    val cardLine: Color,
    val alertRed: Color,
    val alertSoft: Color,
)

val LocalAppPalette = staticCompositionLocalOf { LightPalette }

/** Valores idénticos a las constantes originales: el modo claro queda intacto. */
val LightPalette = AppPalette(
    ink = Color(0xFF4A3B2A),
    inkSubtle = Color(0xFF5C4A36),
    muted = Color(0xFF6E5B48),
    cream = Color(0xFFF7EFDB),
    paper = Color(0xFFFFFBF4),
    paperSubtle = Color(0xFFF3E7D2),
    line = Color(0xFFD9CDB8),
    bannerSubtitle = Color(0xFF2A2118),
    green = Color(0xFF5E9C98),
    greenDark = Color(0xFF2C554F),
    greenSoft = Color(0xFFD7E6E3),
    greenTint = Color(0xFFB7D0CC),
    mint = Color(0xFFE4F0EE),
    ochre = Color(0xFFC4A15A),
    ochreDark = Color(0xFF8C6A2F),
    moss = Color(0xFF6B8F71),
    yellow = Color(0xFFF39C12),
    honey = Color(0xFFF5A623),
    honeySoft = Color(0xFFFFF4DE),
    honeyTint = Color(0xFFFFE7B3),
    orange = Color(0xFFE76235),
    peach = Color(0xFFFFECE3),
    coral = Color(0xFFE85D42),
    sky = Color(0xFFE4F3FA),
    skyBlue = Color(0xFF328CD6),
    skyTint = Color(0xFFCBE7F8),
    lilac = Color(0xFFF1EBFA),
    purple = Color(0xFF7E57C2),
    purpleTint = Color(0xFFDECFF3),
    pinkSoft = Color(0xFFFFEEF3),
    rose = Color(0xFFE04F75),
    beige = Color(0xFFF0E8D9),
    dark = Color(0xFF2A231F),
    darkCard = Color(0xFF332B26),
    cardPaper = Color(0xFFFBF6E9),
    cardLine = Color(0xFFD6C8AA),
    alertRed = Color(0xFFC62828),
    alertSoft = Color(0xFFFFEBEE),
)

/**
 * Tema oscuro: texto y fondos se invierten y los contenedores "pastel" pasan a versiones
 * oscuras para mantener contraste. Los acentos vivos (verde, miel, naranja, violeta...) se
 * conservan porque ya funcionan bien sobre fondos oscuros.
 */
val DarkPalette = AppPalette(
    ink = Color(0xFFFFFBF4),       // Paper
    inkSubtle = Color(0xFFF3E7D2), // PaperSubtle
    muted = Color(0xFFE3D4BC),     // texto secundario claro
    cream = Color(0xFF2A231F),     // Dark
    paper = Color(0xFF332B26),     // DarkCard
    paperSubtle = Color(0xFF4A3B2A), // Ink
    line = Color(0xFF6E5B48),      // Muted
    bannerSubtitle = Color(0xFFE3D4BC),
    green = Color(0xFF5E9C98),
    greenDark = Color(0xFFA9D5CE), // texto verde claro sobre superficies oscuras
    greenSoft = Color(0xFF1F3B37), // contenedor verde oscuro
    greenTint = Color(0xFF2E5A54),
    mint = Color(0xFF213A36),
    ochre = Color(0xFFC4A15A),
    ochreDark = Color(0xFFE3C483),
    moss = Color(0xFF6B8F71),
    yellow = Color(0xFFF39C12),
    honey = Color(0xFFF5A623),
    honeySoft = Color(0xFF4A3617),
    honeyTint = Color(0xFF6B4E1F),
    orange = Color(0xFFE76235),
    peach = Color(0xFF4A2A20),
    coral = Color(0xFFE85D42),
    sky = Color(0xFF173342),
    skyBlue = Color(0xFF328CD6),
    skyTint = Color(0xFF24536B),
    lilac = Color(0xFF322A47),
    purple = Color(0xFF7E57C2),
    purpleTint = Color(0xFF4A3C6B),
    pinkSoft = Color(0xFF482632),
    rose = Color(0xFFE04F75),
    beige = Color(0xFF2E2823),
    dark = Color(0xFF1F1A17),
    darkCard = Color(0xFF332B26),
    cardPaper = Color(0xFF332B26),
    cardLine = Color(0xFF6E5B48),
    alertRed = Color(0xFFE53935),
    alertSoft = Color(0xFF4A2020),
)

/** Texto sobre imágenes claras fijas (header_inicio): no cambia con el tema. */
val OnImageText = Color(0xFF4A3B2A)

// Alias públicos: mismos nombres de siempre, ahora resueltos desde la paleta activa.
@get:Composable val Ink: Color get() = LocalAppPalette.current.ink
@get:Composable val InkSubtle: Color get() = LocalAppPalette.current.inkSubtle
@get:Composable val Muted: Color get() = LocalAppPalette.current.muted
@get:Composable val Cream: Color get() = LocalAppPalette.current.cream
@get:Composable val Paper: Color get() = LocalAppPalette.current.paper
@get:Composable val PaperSubtle: Color get() = LocalAppPalette.current.paperSubtle
@get:Composable val Line: Color get() = LocalAppPalette.current.line
@get:Composable val BannerSubtitle: Color get() = LocalAppPalette.current.bannerSubtitle

@get:Composable val Green: Color get() = LocalAppPalette.current.green
@get:Composable val GreenDark: Color get() = LocalAppPalette.current.greenDark
@get:Composable val GreenSoft: Color get() = LocalAppPalette.current.greenSoft
@get:Composable val GreenTint: Color get() = LocalAppPalette.current.greenTint
@get:Composable val Mint: Color get() = LocalAppPalette.current.mint
@get:Composable val Ochre: Color get() = LocalAppPalette.current.ochre
@get:Composable val OchreDark: Color get() = LocalAppPalette.current.ochreDark
@get:Composable val Moss: Color get() = LocalAppPalette.current.moss

@get:Composable val Yellow: Color get() = LocalAppPalette.current.yellow
@get:Composable val Honey: Color get() = LocalAppPalette.current.honey
@get:Composable val HoneySoft: Color get() = LocalAppPalette.current.honeySoft
@get:Composable val HoneyTint: Color get() = LocalAppPalette.current.honeyTint

@get:Composable val Orange: Color get() = LocalAppPalette.current.orange
@get:Composable val Peach: Color get() = LocalAppPalette.current.peach
@get:Composable val Coral: Color get() = LocalAppPalette.current.coral

@get:Composable val Sky: Color get() = LocalAppPalette.current.sky
@get:Composable val SkyBlue: Color get() = LocalAppPalette.current.skyBlue
@get:Composable val SkyTint: Color get() = LocalAppPalette.current.skyTint

@get:Composable val Lilac: Color get() = LocalAppPalette.current.lilac
@get:Composable val Purple: Color get() = LocalAppPalette.current.purple
@get:Composable val PurpleTint: Color get() = LocalAppPalette.current.purpleTint

@get:Composable val PinkSoft: Color get() = LocalAppPalette.current.pinkSoft
@get:Composable val Rose: Color get() = LocalAppPalette.current.rose

@get:Composable val Beige: Color get() = LocalAppPalette.current.beige
@get:Composable val Dark: Color get() = LocalAppPalette.current.dark
@get:Composable val DarkCard: Color get() = LocalAppPalette.current.darkCard

/** Papel y filete de las tarjetas "notebook". */
@get:Composable val CardPaper: Color get() = LocalAppPalette.current.cardPaper
@get:Composable val CardLine: Color get() = LocalAppPalette.current.cardLine

/** Colores de alerta (antes literales en MarkIcons). */
@get:Composable val AlertRed: Color get() = LocalAppPalette.current.alertRed
@get:Composable val AlertSoft: Color get() = LocalAppPalette.current.alertSoft

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

private val LightColors = lightColorScheme(
    primary = LightPalette.green,
    onPrimary = Color.White,
    primaryContainer = LightPalette.greenSoft,
    onPrimaryContainer = LightPalette.greenDark,
    secondary = LightPalette.orange,
    onSecondary = Color.White,
    secondaryContainer = LightPalette.peach,
    onSecondaryContainer = LightPalette.orange,
    tertiary = LightPalette.honey,
    onTertiary = LightPalette.ink,
    tertiaryContainer = LightPalette.honeySoft,
    onTertiaryContainer = LightPalette.ink,
    background = LightPalette.cream,
    onBackground = LightPalette.ink,
    surface = LightPalette.paper,
    onSurface = LightPalette.ink,
    surfaceVariant = LightPalette.paperSubtle,
    onSurfaceVariant = LightPalette.muted,
    outline = LightPalette.line,
)

private val DarkColors = darkColorScheme(
    primary = DarkPalette.green,
    onPrimary = DarkPalette.dark,
    primaryContainer = DarkPalette.greenSoft,
    onPrimaryContainer = DarkPalette.ink,
    secondary = DarkPalette.honey,
    onSecondary = DarkPalette.dark,
    secondaryContainer = DarkPalette.orange,
    onSecondaryContainer = DarkPalette.paper,
    tertiary = DarkPalette.honey,
    onTertiary = DarkPalette.dark,
    tertiaryContainer = DarkPalette.honeySoft,
    onTertiaryContainer = DarkPalette.ink,
    background = DarkPalette.dark,
    onBackground = DarkPalette.ink,
    surface = DarkPalette.darkCard,
    onSurface = DarkPalette.ink,
    surfaceVariant = DarkPalette.paperSubtle,
    onSurfaceVariant = DarkPalette.muted,
    outline = DarkPalette.muted,
)

/**
 * La tipografía NO fija colores: Material3 aplica `LocalContentColor` (derivado del
 * `colorScheme`) en cada `Text`. Así el mismo estilo sirve al tema claro y al oscuro.
 */
private val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 44.sp),
    headlineMedium = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 38.sp),
    headlineSmall = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 32.sp),
    titleMedium = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 28.sp),
    titleSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun GuiaTheme(darkTheme: Boolean? = null, content: @Composable () -> Unit) {
    // null = seguir el tema del sistema; true/false = preferencia explícita del usuario.
    val dark = darkTheme ?: isSystemInDarkTheme()
    val palette = if (dark) DarkPalette else LightPalette
    CompositionLocalProvider(
        LocalAppPalette provides palette,
        // Los Text sin `color` explícito heredan el color de la paleta activa.
        LocalContentColor provides palette.ink,
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
