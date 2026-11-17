package com.guiaheartopia.app.ui.hobbies

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.R
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted

data class HobbyDoor(
    val route: String,
    val title: String,
    val subtitle: String,
)

/** Sumar una afición es agregar una puerta a esta lista. */
val HobbyDoors: List<HobbyDoor> = listOf(
    HobbyDoor("guide", "Naturaleza", "Peces, aves, insectos y fauna"),
    HobbyDoor("huerto", "Huerto", "Siembras y flores"),
    HobbyDoor("cocina", "Cocina", "Recetas"),
    HobbyDoor("hobbies", "Niveles", "Tus oficios y el nivel de cada uno"),
)

@Composable
fun HobbyHubScreen(onOpen: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Aficiones",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = Ink,
        )
        Text(
            "Elige una afición. Cada tarjeta abre su propia pantalla.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
        HobbyDoors.forEach { door ->
            PixelCard(
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = door.title }
                    .clickable { onOpen(door.route) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    HobbyDoorIcon(door.route, Modifier.size(56.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            door.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Text(door.subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun HobbyDoorIcon(route: String, modifier: Modifier = Modifier) {
    val res = when (route) {
        "guide" -> R.drawable.ic_aficion_naturaleza
        "huerto" -> R.drawable.ic_aficion_huerto
        "cocina" -> R.drawable.ic_aficion_cocina
        else -> R.drawable.ic_aficion_niveles
    }
    Image(painter = painterResource(res), contentDescription = null, modifier = modifier)
}
