package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.PaperSubtle

/**
 * Estado vacío compartido. Separa dos casos que antes se mezclaban:
 * no hay datos todavía ("próximamente") frente a filtros sin coincidencias.
 */
@Composable
fun EmptyStateCard(
    title: String,
    message: String? = null,
    icon: String = "🌱",
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    PixelCard(modifier = modifier.fillMaxWidth(), container = PaperSubtle) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Ink,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
            }
            action?.invoke()
        }
    }
}

/** Tarjeta canónica de "Próximamente" (reemplaza los dos textos duplicados). */
@Composable
fun ComingSoonCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val detail = listOfNotNull(title.takeIf { it.isNotBlank() }, subtitle?.takeIf { it.isNotBlank() })
        .joinToString(" · ")
    EmptyStateCard(
        title = "Próximamente",
        message = detail.ifBlank { null },
        icon = "🌱",
        modifier = modifier,
    )
}
