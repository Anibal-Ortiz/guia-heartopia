package com.guiaheartopia.app.ui.routine

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.domain.RoutineLogic
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.CozyRoutineIcon
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import com.guiaheartopia.app.ui.components.SoftCheck
import com.guiaheartopia.app.ui.components.VerifyBadge
import com.guiaheartopia.app.ui.theme.Dark
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GreenTint
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.PaperSubtle
import com.guiaheartopia.app.ui.theme.PinkSoft
import com.guiaheartopia.app.ui.theme.Rose

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
    val total = ui.routine.tasks.size
    val fraction = ui.routineFraction
    val percent = (fraction * 100).toInt()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Rutina Diaria",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Ink,
                )
                Text(
                    "Se reinicia automáticamente a medianoche",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                CozyRoutineIcon(tint = GreenDark, size = 22.dp, filled = true)
            }
        }

        // Daily Progress Banner Card
        PixelCard(container = Paper) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Progreso de hoy",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        "$done de $total tareas completadas ($percent%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
                val badgeText = when {
                    total == 0 -> "Sin tareas"
                    done == total -> "¡Completado! 🎉"
                    done > 0 -> "En progreso 💪"
                    else -> "Por empezar ☀️"
                }
                val badgeTone = when {
                    done == total && total > 0 -> GreenSoft
                    done > 0 -> HoneySoft
                    else -> PaperSubtle
                }
                VerifyBadge(badgeText, tone = badgeTone)
            }

            PixelProgress(fraction, color = Green, track = GreenSoft)
        }

        // Tasks List
        if (ui.routine.tasks.isEmpty()) {
            PixelCard(container = PaperSubtle) {
                Text(
                    "No tienes tareas en tu rutina todavía. Escribe una abajo para comenzar tu lista del día.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ui.routine.tasks.forEach { task ->
                    val isDone = task.id in ui.routine.completedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x08000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDone) GreenSoft.copy(alpha = 0.5f) else Paper)
                            .border(1.dp, if (isDone) GreenTint else Line, RoundedCornerShape(16.dp))
                            .clickable { onToggle(task.id) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SoftCheck(checked = isDone) { onToggle(task.id) }
                        Text(
                            task.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                            ),
                            color = if (isDone) Muted else Ink,
                        )
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onRemove(task.id) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✕", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Add Task Card
        PixelCard(container = PaperSubtle) {
            Text(
                "Añadir Tarea Personal",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Ej: Regar las flores del huerto") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                )
                PixelButton(
                    text = "Añadir",
                    onClick = {
                        if (draft.isNotBlank()) {
                            onAdd(draft.trim())
                            draft = ""
                        }
                    },
                    container = Green,
                )
            }
        }

        // Restore Action Button
        PixelButton(
            text = "Restaurar tareas de hoy",
            onClick = { confirm = true },
            outlined = true,
            container = Honey,
            modifier = Modifier.fillMaxWidth(),
        )

        if (confirm) {
            AlertDialog(
                onDismissRequest = { confirm = false },
                title = { Text("¿Restaurar el día?", fontWeight = FontWeight.Bold) },
                text = { Text("Se desmarcarán todas las tareas de hoy para que puedas empezar de nuevo. La lista de tareas se mantendrá intacta.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirm = false
                        onRestore()
                    }) {
                        Text("Restaurar", color = GreenDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirm = false }) {
                        Text("Cancelar", color = Muted)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Paper,
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}
