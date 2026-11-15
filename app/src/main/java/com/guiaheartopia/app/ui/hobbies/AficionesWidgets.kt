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
import com.guiaheartopia.app.ui.components.CaptureNetIcon
import com.guiaheartopia.app.ui.components.CertificateMedalIcon
import com.guiaheartopia.app.ui.components.NonCertifiableIcon
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Ochre
import com.guiaheartopia.app.ui.theme.Paper

@Composable
fun AficionesListHeader(
    found: Int,
    total: Int,
    sortByLevel: Boolean,
    onSort: () -> Unit,
    onlyConseguidas: Boolean?,
    onConseguidas: (Boolean?) -> Unit,
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
                selected = sortByLevel,
                onClick = onSort,
                label = {
                    Text(
                        if (sortByLevel) "Nivel" else "Nombre",
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = HoneySoft, selectedLabelColor = Ink),
            )
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
fun ConseguidoToggle(on: Boolean, onToggle: () -> Unit) {
    MarkIconButton(
        on = on,
        description = if (on) "Quitar conseguido" else "Marcar como conseguido",
        onClick = onToggle,
    ) { CaptureNetIcon(on = on) }
}

@Composable
fun CertificadoToggle(on: Boolean, onToggle: () -> Unit) {
    MarkIconButton(
        on = on,
        description = if (on) "Quitar certificado" else "Marcar como certificado",
        onClick = onToggle,
    ) { CertificateMedalIcon(on = on) }
}

@Composable
fun AficionCertifyRow(
    found: Boolean,
    certified: Boolean,
    certifiable: Boolean,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        ConseguidoToggle(found, onToggleFound)
        if (certifiable) {
            CertificadoToggle(certified, onToggleCertified)
        } else {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Paper)
                    .border(1.dp, Line, CircleShape)
                    .clearAndSetSemantics { contentDescription = "No certificable" },
                contentAlignment = Alignment.Center,
            ) {
                NonCertifiableIcon(size = 26.dp)
            }
        }
    }
}

@Composable
fun FavoritaToggle(on: Boolean, onToggle: () -> Unit) {
    MarkIconButton(
        on = on,
        description = if (on) "Quitar favorita" else "Marcar como favorita",
        onClick = onToggle,
    ) {
        Text(if (on) "♥" else "♡", fontSize = 20.sp, color = if (on) Ochre else Ink)
    }
}

@Composable
private fun MarkIconButton(
    on: Boolean,
    description: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (on) HoneySoft else Paper)
            .border(width = if (on) 2.dp else 1.dp, color = if (on) Ochre else Line, shape = CircleShape)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}