package com.guiaheartopia.app.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.VerifiedCatalog
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.VerifyBadge
import com.guiaheartopia.app.ui.theme.Beige
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Orange
import com.guiaheartopia.app.ui.theme.Paper
import java.time.LocalDate

private enum class MapFilter(val label: String) { ALL("Todo"), FLURITA("flurita"), LIULIU("Liuliu"), FEEDERS("Comederos") }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(zoneId: String) {
    var filter by remember { mutableStateOf(MapFilter.ALL) }
    var scale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val today = LocalDate.now(runCatching { java.time.ZoneId.of(zoneId) }.getOrElse { java.time.ZoneId.systemDefault() })
    val spot = VerifiedCatalog.novemberSpot(today)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Mapa", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Dibujo original de 12 parcelas, un bosque y unas ruinas. No es el mapa del juego ni usa sus coordenadas.",
            style = MaterialTheme.typography.bodyMedium,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MapFilter.entries.forEach { item ->
                FilterChip(selected = filter == item, onClick = { filter = item }, label = { Text(item.label) })
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(Beige, RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, drag, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3.5f)
                        pan += drag
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                fun ox(x: Float) = pan.x + (x - w / 2f) * scale + w / 2f
                fun oy(y: Float) = pan.y + (y - h / 2f) * scale + h / 2f
                fun s(v: Float) = v * scale
                drawRoundRect(Color(0xFFD7EBD4), Offset(ox(w * 0.62f), oy(h * 0.08f)), Size(s(w * 0.3f), s(h * 0.28f)), CornerRadius(40f, 40f))
                drawRoundRect(Color(0xFFD9D3C8), Offset(ox(w * 0.08f), oy(h * 0.62f)), Size(s(w * 0.28f), s(h * 0.24f)), CornerRadius(28f, 28f))
                val showHouses = filter == MapFilter.ALL || filter == MapFilter.FLURITA || filter == MapFilter.LIULIU
                if (showHouses) {
                    for (i in 0 until 12) {
                        val col = i % 4
                        val row = i / 4
                        val left = w * (0.12f + col * 0.18f)
                        val top = h * (0.18f + row * 0.18f)
                        val label = (i + 1).toString()
                        val hitLiuliu = spot?.first == label && (filter == MapFilter.ALL || filter == MapFilter.LIULIU)
                        val hitFluorite = spot?.second == label && (filter == MapFilter.ALL || filter == MapFilter.FLURITA)
                        val fill = when {
                            hitLiuliu && hitFluorite -> Orange
                            hitLiuliu -> GreenSoft
                            hitFluorite -> Color(0xFFFFE1C4)
                            else -> Paper
                        }
                        drawRoundRect(fill, Offset(ox(left), oy(top)), Size(s(w * 0.14f), s(h * 0.12f)), CornerRadius(16f, 16f))
                        drawRoundRect(Ink, Offset(ox(left), oy(top)), Size(s(w * 0.14f), s(h * 0.12f)), CornerRadius(16f, 16f), style = Stroke(2f))
                    }
                }
            }
        }
        Text("Pellizca para ampliar. El bosque está arriba a la derecha y las ruinas abajo a la izquierda.", style = MaterialTheme.typography.bodySmall)
        if (filter == MapFilter.FEEDERS) {
            PixelCard {
                Text("Comederos", style = MaterialTheme.typography.titleMedium)
                Text("No hay coordenadas en las que coincidan las guías. Las fichas guardan la comida contrastada y dejan el lugar pendiente.", style = MaterialTheme.typography.bodyMedium)
            }
        } else if (spot == null) {
            VerifyBadge("Pendiente de verificar")
            Text("Para hoy no hay una ubicación de flurita o Liuliu contrastada en dos fuentes. Noviembre 2026 sí está cargado.", style = MaterialTheme.typography.bodyMedium)
        } else {
            PixelCard {
                Text("Hoy en las guías", style = MaterialTheme.typography.titleMedium)
                if (filter != MapFilter.FLURITA) Text("Liuliu: ${VerifiedCatalog.spotLabel(spot.first)}", color = Green)
                if (filter != MapFilter.LIULIU) Text("flurita: ${VerifiedCatalog.spotLabel(spot.second)}", color = Orange)
                Text(VerifiedCatalog.SPOT_SOURCES, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
