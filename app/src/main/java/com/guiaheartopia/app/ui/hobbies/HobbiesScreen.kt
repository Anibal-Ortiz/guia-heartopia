package com.guiaheartopia.app.ui.hobbies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.HobbyProgress
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import com.guiaheartopia.app.ui.components.VerifyBadge

@Composable
fun HobbiesScreen(
    hobbies: List<Hobby>,
    onChange: (id: String, level: Int?, max: Int?, note: String?) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Aficiones", style = MaterialTheme.typography.headlineSmall)
        VerifyBadge("Progreso tuyo")
        Text(
            "Cocina, jardinería, mascotas y el resto son marcas que tú guardas.",
            style = MaterialTheme.typography.bodyMedium,
        )
        hobbies.forEach { hobby ->
            HobbyCard(hobby, onChange, onRemove)
        }
        OutlinedTextField(draft, { draft = it }, label = { Text("Otra afición") }, modifier = Modifier.fillMaxWidth())
        PixelButton("Añadir hobby", onClick = {
            onAdd(draft)
            draft = ""
        })
    }
}

@Composable
private fun HobbyCard(
    hobby: Hobby,
    onChange: (id: String, level: Int?, max: Int?, note: String?) -> Unit,
    onRemove: (String) -> Unit,
) {
    var note by remember(hobby.id, hobby.note) { mutableStateOf(hobby.note) }
    PixelCard {
        Text(hobby.name, style = MaterialTheme.typography.titleMedium)
        Text("Nivel ${hobby.level} de ${hobby.maxLevel}", style = MaterialTheme.typography.bodyMedium)
        PixelProgress(HobbyProgress.fraction(hobby.level, hobby.maxLevel))
        Row {
            TextButton(onClick = { onChange(hobby.id, hobby.level - 1, null, null) }) { Text("−") }
            TextButton(onClick = { onChange(hobby.id, hobby.level + 1, null, null) }) { Text("+") }
            TextButton(onClick = { onChange(hobby.id, null, hobby.maxLevel + 1, null) }) { Text("Subir tope") }
            TextButton(onClick = { onRemove(hobby.id) }) { Text("Quitar") }
        }
        OutlinedTextField(
            note,
            { note = it },
            label = { Text("Nota") },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { onChange(hobby.id, null, null, note) }) { Text("Guardar nota") }
    }
}
