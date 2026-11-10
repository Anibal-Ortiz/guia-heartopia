package com.guiaheartopia.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.PhotoCreditEntry
import com.guiaheartopia.app.data.PhotoCredits
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted

private fun openUrl(context: Context, url: String) {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)))
    }
}

@Composable
fun PhotoCreditsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val file = remember(revision) { PhotoCredits.load(context) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Volver") }
        com.guiaheartopia.app.ui.components.NotebookTitle(
            "Créditos de las fotos",
            com.guiaheartopia.app.ui.components.NotebookMark.BIRD,
        )
        when {
            file == null || file.entries.isEmpty() -> {
                Text(
                    "Busca actualizaciones para ver los créditos de las fotos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
            else -> {
                PhotoCredits.grouped(file.entries).forEach { (section, items) ->
                    PixelCard {
                        Text(section, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        items.forEach { entry ->
                            PhotoCreditRow(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCreditRow(entry: PhotoCreditEntry) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(entry.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
        if (entry.scientificName.isNotBlank()) {
            Text(
                entry.scientificName,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = Muted,
            )
        }
        Text(
            "Foto: ${PhotoCredits.authorLabel(entry.author)} · ${entry.license.ifBlank { "—" }}",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
        )
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (entry.licenseUrl.isNotBlank()) {
                Text(
                    "Licencia",
                    style = MaterialTheme.typography.labelLarge,
                    color = Green,
                    modifier = Modifier.clickable { openUrl(context, entry.licenseUrl) },
                )
            }
            if (entry.sourcePage.isNotBlank()) {
                Text(
                    "Fuente",
                    style = MaterialTheme.typography.labelLarge,
                    color = Green,
                    modifier = Modifier.clickable { openUrl(context, entry.sourcePage) },
                )
            }
        }
        if (entry.nota.isNotBlank()) {
            Text(entry.nota, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

@Composable
fun PhotoCreditCaption(entry: PhotoCreditEntry, modifier: Modifier = Modifier) {
    Text(
        PhotoCredits.caption(entry),
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = Muted,
    )
}
