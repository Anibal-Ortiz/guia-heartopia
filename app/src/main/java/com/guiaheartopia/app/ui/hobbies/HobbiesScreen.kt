package com.guiaheartopia.app.ui.hobbies

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.HobbyProgress
import com.guiaheartopia.app.ui.components.CozyHobbiesIcon
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import com.guiaheartopia.app.ui.components.VerifyBadge
import com.guiaheartopia.app.ui.theme.Dark
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GreenTint
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.HoneyTint
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.PaperSubtle
import com.guiaheartopia.app.ui.theme.Peach
import com.guiaheartopia.app.ui.theme.Yellow

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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Aficiones & Habilidades",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Ink,
                )
                Text(
                    "Registro personal de niveles de tu personaje",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(HoneySoft),
                contentAlignment = Alignment.Center,
            ) {
                CozyHobbiesIcon(tint = Honey, size = 22.dp, filled = true)
            }
        }

        // Hobbies list
        hobbies.forEach { hobby ->
            HobbyCard(hobby, onChange, onRemove)
        }

        // Add hobby card
        PixelCard(container = PaperSubtle) {
            Text(
                "Añadir Nueva Afición",
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
                    placeholder = { Text("Ej: Pintura, Minería...") },
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
                    container = Honey,
                    contentColor = Ink,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun hobbyEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        "cocina" in lower -> "🍳"
        "pesca" in lower -> "🎣"
        "jardin" in lower || "huerto" in lower || "flor" in lower -> "🌱"
        "insect" in lower || "bicho" in lower -> "🦋"
        "pájaro" in lower || "ave" in lower -> "🐦"
        "mascota" in lower || "animal" in lower -> "🐾"
        "miner" in lower || "piedra" in lower -> "⛏️"
        "música" in lower || "canto" in lower -> "🎵"
        "art" in lower || "pint" in lower -> "🎨"
        else -> "⭐"
    }
}

@Composable
private fun HobbyCard(
    hobby: Hobby,
    onChange: (id: String, level: Int?, max: Int?, note: String?) -> Unit,
    onRemove: (String) -> Unit,
) {
    var note by remember(hobby.id, hobby.note) { mutableStateOf(hobby.note) }
    val emoji = hobbyEmoji(hobby.name)
    val fraction = HobbyProgress.fraction(hobby.level, hobby.maxLevel)
    val isMaxed = hobby.level >= hobby.maxLevel

    PixelCard(container = Paper) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HoneySoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, fontSize = 20.sp)
                }
                Column {
                    Text(
                        hobby.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        "Nivel ${hobby.level} de ${hobby.maxLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
            if (isMaxed) {
                VerifyBadge("¡Tope! ⭐", tone = HoneySoft)
            } else {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onRemove(hobby.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✕", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        PixelProgress(
            fraction,
            color = if (isMaxed) Honey else Green,
            track = if (isMaxed) HoneySoft else GreenSoft,
        )

        // Level Stepper Row
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Decrement Button
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PaperSubtle)
                        .border(1.dp, Line, CircleShape)
                        .clickable { onChange(hobby.id, hobby.level - 1, null, null) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                }

                Text(
                    "Nv. ${hobby.level}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GreenDark,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )

                // Increment Button
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GreenSoft)
                        .border(1.dp, GreenTint, CircleShape)
                        .clickable { onChange(hobby.id, hobby.level + 1, null, null) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton(
                    text = "Subir tope (+1)",
                    onClick = { onChange(hobby.id, null, hobby.maxLevel + 1, null) },
                    outlined = true,
                    container = Paper,
                )
            }
        }

        // Note Input
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Añadir nota personal...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
            PixelButton(
                text = "Guardar",
                onClick = { onChange(hobby.id, null, null, note) },
                outlined = true,
                container = Green,
            )
        }
    }
}
