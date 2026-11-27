package com.guiaheartopia.app.ui.codes

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.domain.RedeemCode
import com.guiaheartopia.app.domain.RedeemCodes
import com.guiaheartopia.app.ui.components.EmptyStateCard
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.hobbies.ConseguidoToggle
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
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

/** Billete con los bordes laterales picados (dientes de sierra), como un golden ticket. */
private val TicketShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val teeth = 10
    val step = h / teeth
    val depth = w * 0.012f
    moveTo(0f, 0f)
    lineTo(w, 0f)
    for (i in 0 until teeth) {
        val y = step * i
        lineTo(w - depth, y + step / 2)
        lineTo(w, y + step)
    }
    lineTo(0f, h)
    for (i in teeth - 1 downTo 0) {
        val y = step * i
        lineTo(depth, y + step / 2)
        lineTo(0f, y)
    }
    close()
}

/** Cada código es un cupón dorado de dos líneas: código/estado y recompensa/acciones. */
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
    val edge = if (expired) CouponExpiredEdge else CouponGoldEdge
    val detail = listOfNotNull(
        code.recompensa.takeIf { it.isNotBlank() },
        whenLabel,
    ).joinToString(" · ")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TicketShape)
            .background(couponBrush(expired))
            .border(1.dp, edge, TicketShape),
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

        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    code.code,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp,
                        color = CouponInk,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                CouponStatusChip(
                    text = when {
                        expired -> "VENCIDO"
                        redeemed -> "CANJEADO ✓"
                        else -> "VIGENTE"
                    },
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    detail,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = CouponInk.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Mismo botón de "conseguido" que en bichos/peces; copiar es independiente.
                ConseguidoToggle(
                    on = redeemed,
                    onToggle = { onRedeemed(code.code, !redeemed) },
                    compact = true,
                )
                CouponCopyButton(
                    onClick = {
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        clipboard.setPrimaryClip(ClipData.newPlainText("código", code.code))
                        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                    },
                )
            }
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
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = CouponCream)
    }
}

/** Botoncito circular para copiar el código, a juego con el billete. */
@Composable
private fun CouponCopyButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CouponCream.copy(alpha = 0.7f))
            .border(1.dp, CouponInk, CircleShape)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "Copiar código"
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(com.guiaheartopia.app.R.drawable.ic_copy),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(CouponInk),
        )
    }
}

private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))