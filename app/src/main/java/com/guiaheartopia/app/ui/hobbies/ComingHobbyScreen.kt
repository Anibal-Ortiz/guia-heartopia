package com.guiaheartopia.app.ui.hobbies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.domain.HobbyContent
import com.guiaheartopia.app.domain.HobbyEntry
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.SearchDrafts
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted

@Composable
fun ComingHobbyScreen(
    title: String,
    subtitle: String,
    paths: List<String>,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val revision = contentRevision()
    var queryField by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(SearchDrafts.read("hobby:$title") ?: ""))
    }
    val query = queryField.text
    val items = remember(revision, paths) {
        val library = AndroidContentLibrary(context)
        paths.flatMap { path ->
            HobbyContent.parse(library.effectiveBytes(path)?.decodeToString())
        }
    }
    val shown = HobbyContent.search(items, query)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Atrás") }
        Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Ink)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        OutlinedTextField(
            value = queryField,
            onValueChange = {
                queryField = it
                SearchDrafts.write("hobby:$title", it.text)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Buscar") },
        )
        if (items.isEmpty()) {
            PixelCard {
                Text("Próximamente", style = MaterialTheme.typography.titleMedium, color = Ink)
            }
        } else if (shown.isEmpty()) {
            Text("Nada coincide con la búsqueda.", color = Muted)
        } else {
            shown.forEach { entry -> HobbyEntryCard(entry) }
        }
    }
}

@Composable
private fun HobbyEntryCard(entry: HobbyEntry) {
    PixelCard {
        Text(entry.nombre, style = MaterialTheme.typography.titleMedium, color = Ink)
        entry.temporada?.takeIf { it.isNotBlank() }?.let {
            Text("Temporada: $it", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        entry.tiempoCrecimiento?.takeIf { it.isNotBlank() }?.let {
            Text("Tiempo: $it", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        if (entry.ingredientes.isNotEmpty()) {
            Text(
                "Ingredientes: ${entry.ingredientes.joinToString()}",
                color = Ink,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        entry.precioVenta?.takeIf { it.isNotBlank() }?.let {
            Text("Precio de venta: $it", color = Ink, style = MaterialTheme.typography.bodySmall)
        }
    }
}
