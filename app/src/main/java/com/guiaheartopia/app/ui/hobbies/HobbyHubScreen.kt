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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.R
import com.guiaheartopia.app.data.AficionesCatalog
import com.guiaheartopia.app.data.HeartodexCatalog
import com.guiaheartopia.app.domain.AficionesLevelSummary
import com.guiaheartopia.app.domain.CreatureAvailability
import com.guiaheartopia.app.domain.GuideSections
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted

data class HobbyDoor(
    val route: String,
    val title: String,
    val subtitle: String,
    val lockedRoute: String? = null,
)

/** Sumar una afición es agregar una puerta a esta lista. */
val HobbyDoors: List<HobbyDoor> = listOf(
    HobbyDoor("guide", "Naturaleza", "Peces, aves, insectos y fauna", "guide/pendiente-nivel"),
    HobbyDoor("huerto", "Huerto", "Siembras y flores", "huerto/pendiente-nivel"),
    HobbyDoor("cocina", "Cocina", "Recetas", "cocina/pendiente-nivel"),
    HobbyDoor("hobbies", "Niveles", "Tus oficios y el nivel de cada uno"),
)

@Composable
fun HobbyHubScreen(ui: GuideUi, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val revision = contentRevision()
    val catalog = remember(revision) { HeartodexCatalog.load(context) }
    val aficiones = remember(revision) { AficionesCatalog.load(context) }
    val natureLocked = remember(catalog, ui.hobbies) {
        GuideSections.dexCategories.sumOf { category ->
            val hobbyLevel = CreatureAvailability.hobbyLevel(category, ui.hobbies) ?: 0
            AficionesLevelSummary.lockedCount(
                catalog.filter { it.category == category }.mapNotNull { it.level },
                hobbyLevel,
            )
        }
    }
    val siembraLevel = ui.hobbies.firstOrNull { it.id == "hob-siembra" }?.level ?: 0
    val huertoLocked = remember(aficiones, siembraLevel) {
        AficionesLevelSummary.lockedCount(aficiones.siembras.map { it.nivelSiembra }, siembraLevel) +
            AficionesLevelSummary.lockedCount(aficiones.flores.mapNotNull { it.nivelSiembra }, siembraLevel)
    }
    val cocinaLevel = ui.hobbies.firstOrNull { it.id == "hob-cocina" }?.level ?: 0
    val cocinaLocked = remember(aficiones, cocinaLevel) {
        AficionesLevelSummary.lockedCount(aficiones.recetas.map { it.nivelCocina }, cocinaLevel)
    }
    val lockedByRoute = mapOf(
        "guide" to natureLocked,
        "huerto" to huertoLocked,
        "cocina" to cocinaLocked,
    )
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
            val locked = lockedByRoute[door.route] ?: 0
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
                        if (locked > 0 && door.lockedRoute != null) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "$locked fichas piden más nivel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted,
                                )
                                TextButton(onClick = { onOpen(door.lockedRoute) }) { Text("Ver lista") }
                            }
                        }
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