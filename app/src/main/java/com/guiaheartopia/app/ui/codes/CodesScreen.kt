package com.guiaheartopia.app.ui.codes

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.domain.RedeemCode
import com.guiaheartopia.app.domain.RedeemCodes
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CodesScreen(
    seen: List<String>,
    redeemed: Set<String>,
    onSeen: (List<String>) -> Unit,
    onRedeemed: (String, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val revision = contentRevision()
    val codes = remember(revision) {
        val raw = AndroidContentLibrary(context).effectiveBytes(RedeemCodes.PATH)?.decodeToString()
        RedeemCodes.parse(raw)
    }
    val today = LocalDate.now()
    val active = RedeemCodes.active(codes, today)
    val expired = RedeemCodes.expired(codes, today)
    LaunchedEffect(active.map { it.code }, seen) {
        val next = (seen + active.map { it.code }).distinct()
        if (next.toSet() != seen.toSet()) onSeen(next)
    }
    var showExpired by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Códigos",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = Ink,
        )
        Text("Vigentes", style = MaterialTheme.typography.titleMedium, color = Ink)
        if (active.isEmpty()) {
            Text("No hay códigos activos por ahora", color = Muted, style = MaterialTheme.typography.bodyLarge)
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
    PixelCard {
        Text(
            code.code,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
            color = Ink,
        )
        if (code.recompensa.isNotBlank()) {
            Text(code.recompensa, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
        whenLabel?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Muted) }
        code.fuente?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        PixelButton(
            text = if (redeemed) "Ya canjeado ✓" else "Marcar como canjeado",
            onClick = { onRedeemed(code.code, !redeemed) },
            outlined = !redeemed,
            modifier = Modifier.fillMaxWidth(),
        )
        PixelButton(
            text = "Copiar",
            onClick = {
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                clipboard.setPrimaryClip(ClipData.newPlainText("código", code.code))
                Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
            },
            outlined = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))