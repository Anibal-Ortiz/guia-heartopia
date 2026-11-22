package com.guiaheartopia.app.ui.hobbies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.ui.components.CertificadoMarkIcon
import com.guiaheartopia.app.ui.components.ConseguidoMarkIcon
import com.guiaheartopia.app.ui.components.MarkIconButton
import com.guiaheartopia.app.ui.components.NoCertificableMarkButton
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Paper

@Composable
fun AficionesListHeader(
    found: Int,
    total: Int,
    onlyConseguidas: Boolean?,
    onConseguidas: (Boolean?) -> Unit,
    onlyCertificadas: Boolean? = null,
    onCertificadas: (Boolean?) -> Unit = {},
    extraChips: (@Composable () -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Conseguidas $found/$total",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = Muted,
        )
        val scroll = rememberScrollState()
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                selected = onlyConseguidas == true,
                onClick = { onConseguidas(if (onlyConseguidas == true) null else true) },
                label = {
                    Text("Hechas", maxLines = 1, overflow = TextOverflow.Clip, style = MaterialTheme.typography.labelMedium)
                },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenSoft, selectedLabelColor = GreenDark),
            )
            FilterChip(
                selected = onlyConseguidas == false,
                onClick = { onConseguidas(if (onlyConseguidas == false) null else false) },
                label = {
                    Text("Pendientes", maxLines = 1, overflow = TextOverflow.Clip, style = MaterialTheme.typography.labelMedium)
                },
            )
            FilterChip(
                selected = onlyCertificadas == true,
                onClick = { onCertificadas(if (onlyCertificadas == true) null else true) },
                label = {
                    Text("Certificadas", maxLines = 1, overflow = TextOverflow.Clip, style = MaterialTheme.typography.labelMedium)
                },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenSoft, selectedLabelColor = GreenDark),
            )
            extraChips?.invoke()
        }
    }
}

@Composable
fun StarPriceRow(
    prices: List<Int>,
    modifier: Modifier = Modifier,
    prefix: String = "",
) {
    if (prices.isEmpty()) return
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (prefix.isNotBlank()) {
            Text(prefix, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        (1..5).forEach { star ->
            val price = prices.getOrNull(star - 1)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("★$star", fontSize = 11.sp, color = Muted)
                Text(
                    price?.toString() ?: "—",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
fun ConseguidoToggle(on: Boolean, onToggle: () -> Unit, compact: Boolean = false) {
    MarkIconButton(
        on = on,
        description = if (on) "Quitar conseguido" else "Marcar como conseguido",
        onClick = onToggle,
        buttonSize = if (compact) 36.dp else 48.dp,
    ) { ConseguidoMarkIcon(on = on, size = if (compact) 20.dp else 26.dp) }
}

@Composable
fun CertificadoToggle(on: Boolean, onToggle: () -> Unit, compact: Boolean = false) {
    MarkIconButton(
        on = on,
        description = if (on) "Quitar certificado" else "Marcar como certificado",
        onClick = onToggle,
        buttonSize = if (compact) 36.dp else 48.dp,
    ) { CertificadoMarkIcon(on = on, size = if (compact) 20.dp else 26.dp) }
}

@Composable
fun AficionCertifyRow(
    found: Boolean,
    certified: Boolean,
    certifiable: Boolean,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
    compact: Boolean = false,
    vertical: Boolean = false,
) {
    val gap = if (compact) 6.dp else 8.dp
    val marks: @Composable () -> Unit = {
        ConseguidoToggle(found, onToggleFound, compact)
        if (certifiable) {
            CertificadoToggle(certified, onToggleCertified, compact)
        } else {
            NoCertificableMarkButton(size = if (compact) 20.dp else 26.dp, buttonSize = if (compact) 36.dp else 48.dp)
        }
    }
    if (vertical) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(gap),
            content = { marks() },
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically, content = { marks() })
    }
}

