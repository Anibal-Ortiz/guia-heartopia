package com.guiaheartopia.app.ui.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.RoutineLogic
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.SoftCheck
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress

@Composable
fun RoutineScreen(
    ui: GuideUi,
    onToggle: (String) -> Unit,
    onRestore: () -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    val done = RoutineLogic.completedCount(ui.routine)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Rutina de hoy", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Se desmarca sola cuando cambia la fecha local. Puedes restaurarla a mano si quieres empezar el día de nuevo.",
            style = MaterialTheme.typography.bodyMedium,
        )
        PixelCard {
            Text("Rutina de hoy", style = MaterialTheme.typography.titleMedium)
            Text("$done de ${ui.routine.tasks.size} completadas", color = Green, style = MaterialTheme.typography.titleSmall)
            Text("Se reinicia cuando cambia la fecha local.", style = MaterialTheme.typography.bodySmall)
            PixelProgress(ui.routineFraction)
        }
        if (ui.routine.tasks.isEmpty()) {
            PixelCard {
                Text("No hay tareas. Añade una o cierra y vuelve a abrir para recuperar las sugerencias solo si nunca se guardó la lista.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        ui.routine.tasks.forEach { task ->
            PixelCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftCheck(task.id in ui.routine.completedIds) { onToggle(task.id) }
                    Text(task.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { onRemove(task.id) }) { Text("Quitar") }
                }
            }
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("Nueva tarea") },
            modifier = Modifier.fillMaxWidth(),
        )
        PixelButton("Añadir tarea", onClick = {
            onAdd(draft)
            draft = ""
        })
        PixelButton("Restaurar tareas del día", onClick = { confirm = true }, container = com.guiaheartopia.app.ui.theme.Honey)
        if (confirm) {
            AlertDialog(
                onDismissRequest = { confirm = false },
                title = { Text("Restaurar el día") },
                text = { Text("Se desmarcan todas las tareas de hoy. La lista se conserva.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirm = false
                        onRestore()
                    }) { Text("Restaurar") }
                },
                dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } },
            )
        }
    }
}
