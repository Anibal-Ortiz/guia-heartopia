package com.guiaheartopia.app.ui.codes

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.domain.RedeemCode
import com.guiaheartopia.app.domain.RedeemCodes
import com.guiaheartopia.app.ui.components.EmptyStateCard
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.theme.Ink
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CodesScreen(
    seen: List<String>,
    redeemed: Set<String>,
    onSeen: (List<String>) -> Unit,
    onRedeemed: (String, Boolean) -> Unit,
    zoneId: String = "",
) {
    val context = LocalContext.current
    val revision = contentRevision()
    val codes = remember(revision) {
        val raw = AndroidContentLibrary(context).effectiveBytes(RedeemCodes.PATH)?.decodeToString()
        RedeemCodes.parse(raw)
    }
    val zone = runCatching { ZoneId.of(zoneId) }.getOrElse { ZoneId.systemDefault() }
    val today = LocalDate.now(zone)
    val active = RedeemCodes.active(codes, today)
    val expired = RedeemCodes.expired(codes, today)
    LaunchedEffect(active.map { it.code }, seen) {
        val next = (seen + active.map { it.code }).distinct()
        if (next.toSet() != seen.toSet()) onSeen(next)
    }
    var showExpired by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
    Box(Modifier.graphicsLayer { clip = false }.padding(16.dp)) {
        SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_codigos,
            title = "Códigos",
            subtitle = "Canjea tus recompensas",
            imageOffsetY = 96.dp,
            imageScale = 2.2f,
        )
    }
    Column(
        Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Vigentes", style = MaterialTheme.typography.titleMedium, color = Ink)
        if (active.isEmpty()) {
            EmptyStateCard(
                title = "Sin códigos activos",
                message = "No hay códigos vigentes por ahora. Vuelve a mirar pronto.",
                icon = "🎟️",
            )
        } else {
            active.forEach { code ->
                CodeCard(
                    code = code,
                    expired = false,
                    redeemed = code.code in redeemed,
                    onRedeemed = onRedeemed,
                )
            }
        }
        if (expired.isNotEmpty()) {
            PixelButton(
                text = if (showExpired) "Ocultar vencidos" else "Ver vencidos (${expired.size})",
                onClick = { showExpired = !showExpired },
                outlined = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (showExpired) {
                Text("Vencidos", style = MaterialTheme.typography.titleMedium, color = Ink)
                expired.forEach { code ->
                    CodeCard(
                        code = code,
                        expired = true,
                        redeemed = code.code in redeemed,
                        onRedeemed = onRedeemed,
                    )
                }
            }
        }
    }
    }
}

// Paleta fija del cupón: un billete dorado es un objeto físico, así que se mantiene
// igual en tema claro y oscuro (con texto oscuro encima para garantizar contraste).
private val CouponCream = Color(0xFFFFF7DC)
private val CouponGold = Color(0xFFF4D27A)
private val CouponGoldDeep = Color(0xFFD9A73F)
private val CouponGoldEdge = Color(0xFF9A6E18)
private val CouponInk = Color(0xFF4A3311)
private val CouponExpiredEdge = Color(0xFF8A7A63)

private fun couponBrush(expired: Boolean) = Brush.linearGradient(
    colors = if (expired) {
        listOf(Color(0xFFF1EADA), Color(0xFFD8CDB9), Color(0xFFC6B99F))
    } else {
        listOf(CouponCream, CouponGold, CouponGoldDeep, CouponGold)
    },
)

/** Cada código se presenta como un cupón dorado con muescas y línea de perforación. */
@Composable
private fun CodeCard(
    code: RedeemCode,
    expired: Boolean,
    redeemed: Boolean,
    onRedeemed: (String, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val until = RedeemCodes.parseDate(code.fechaVencimiento)
    val whenLabel = when {
        until != null && expired -> "Venció el ${formatDate(until)}"
        until != null -> "Vence el ${formatDate(until)}"
        else -> null
    }
    val screenBg = MaterialTheme.colorScheme.background
    val edge = if (expired) CouponExpiredEdge else CouponGoldEdge
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(couponBrush(expired))
            .border(2.dp, edge, shape),
    ) {
        // Brillo diagonal sutil, efecto lámina dorada, por detrás del contenido.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.10f),
                        ),
                    ),
                ),
        )

        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "🎟️  CUPÓN DE RECOMPENSA",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = CouponInk,
                )
                CouponStatusChip(
                    text = when {
                        expired -> "VENCIDO"
                        redeemed -> "CANJEADO ✓"
                        else -> "VIGENTE"
                    },
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                code.code,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    color = CouponInk,
                ),
            )
            if (code.recompensa.isNotBlank()) {
                Text(code.recompensa, style = MaterialTheme.typography.bodyLarge, color = CouponInk)
            }
            whenLabel?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = CouponInk.copy(alpha = 0.8f))
            }
            code.fuente?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = CouponInk.copy(alpha = 0.7f))
            }

            Spacer(Modifier.height(12.dp))
            CouponPerforation(screenBg = screenBg, edge = edge)
            Spacer(Modifier.height(12.dp))

            // Acciones independientes: copiar nunca cambia el estado de canjeo.
            CouponButton(
                text = if (redeemed) "Canjeado ✓ (tocar para desmarcar)" else "Marcar como canjeado",
                onClick = { onRedeemed(code.code, !redeemed) },
                filled = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            CouponButton(
                text = "Copiar código",
                onClick = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    clipboard.setPrimaryClip(ClipData.newPlainText("código", code.code))
                    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                },
                filled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CouponStatusChip(text: String) {
    val chipShape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(chipShape)
            .background(CouponInk)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = CouponCream)
    }
}

/** Línea punteada con dos muescas laterales que simulan el desgarro del ticket. */
@Composable
private fun CouponPerforation(screenBg: Color, edge: Color) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = (-18).dp)
            .height(18.dp),
    ) {
        val cy = size.height / 2
        drawLine(
            color = edge.copy(alpha = 0.75f),
            start = Offset(0f, cy),
            end = Offset(size.width, cy),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
        )
        val r = 10.dp.toPx()
        listOf(Offset(0f, cy), Offset(size.width, cy)).forEach { center ->
            drawCircle(color = screenBg, radius = r, center = center)
            drawCircle(
                color = edge,
                radius = r,
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}

@Composable
private fun CouponButton(
    text: String,
    onClick: () -> Unit,
    filled: Boolean,
    modifier: Modifier = Modifier,
) {
    val buttonShape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(buttonShape)
            .background(if (filled) CouponInk else Color.Transparent)
            .border(1.5.dp, CouponInk, buttonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (filled) CouponCream else CouponInk,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))