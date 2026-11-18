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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.theme.Beige
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Orange
import com.guiaheartopia.app.ui.theme.Paper
import java.time.LocalDate
import java.time.ZoneId

private enum class MapFilter(val label: String) { ALL("Todo"), FLURITA("flurita"), LIULIU("Liuliu"), FEEDERS("Comederos") }

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 3.5f

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(zoneId: String) {
    var filter by remember { mutableStateOf(MapFilter.ALL) }
    // El zoom y el desplazamiento sobreviven a recomposiciones y cambios de configuración.
    var scale by rememberSaveable { mutableFloatStateOf(MIN_SCALE) }
    var panX by rememberSaveable { mutableFloatStateOf(0f) }
    var panY by rememberSaveable { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val pan = remember(panX, panY) { Offset(panX, panY) }
    val gesture = rememberUpdatedState(Triple(scale, pan, canvasSize))

    val zone = runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())
    val today = LocalDate.now(zone)
    val spot = com.guiaheartopia.app.domain.ResourceCalendar.on(today)
    val textMeasurer = rememberTextMeasurer()

    fun boundedPan(value: Offset, currentScale: Float, size: Size): Offset {
        if (currentScale <= MIN_SCALE || size == Size.Zero) return Offset.Zero
        val maxX = (currentScale - MIN_SCALE) * size.width / 2f
        val maxY = (currentScale - MIN_SCALE) * size.height / 2f
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_mapa,
            title = "Mapa",
            subtitle = "Parcelas, bosque y ruinas",
        )
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
                .onSizeChanged { size ->
                    val next = Size(size.width.toFloat(), size.height.toFloat())
                    if (next != canvasSize) canvasSize = next
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, drag, zoom, _ ->
                        val (currentScale, currentPan, size) = gesture.value
                        val nextScale = (currentScale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                        val factor = if (currentScale == 0f) 1f else nextScale / currentScale
                        val desired = Offset(
                            currentPan.x * factor + drag.x,
                            currentPan.y * factor + drag.y,
                        )
                        val bounded = boundedPan(desired, nextScale, size)
                        scale = nextScale
                        panX = bounded.x
                        panY = bounded.y
                    }
                },
        ) {
            val mapInk = Ink
            val mapOrange = Orange
            val mapGreenSoft = GreenSoft
            val mapPaper = Paper
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                fun ox(x: Float) = pan.x + (x - w / 2f) * scale + w / 2f
                fun oy(y: Float) = pan.y + (y - h / 2f) * scale + h / 2f
                fun s(v: Float) = v * scale
                drawRoundRect(Color(0xFFD7EBD4), Offset(ox(w * 0.62f), oy(h * 0.08f)), Size(s(w * 0.3f), s(h * 0.28f)), CornerRadius(40f, 40f))
                drawRoundRect(Color(0xFFD9D3C8), Offset(ox(w * 0.08f), oy(h * 0.62f)), Size(s(w * 0.28f), s(h * 0.24f)), CornerRadius(28f, 28f))
                val showHouses = filter == MapFilter.ALL || filter == MapFilter.FLURITA || filter == MapFilter.LIULIU
                val labelStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = mapInk)
                if (showHouses) {
                    for (i in 0 until 12) {
                        val col = i % 4
                        val row = i / 4
                        val left = w * (0.12f + col * 0.18f)
                        val top = h * (0.18f + row * 0.18f)
                        val label = (i + 1).toString()
                        val hitLiuliu = spot.liuliu == label && (filter == MapFilter.ALL || filter == MapFilter.LIULIU)
                        val hitFluorite = spot.flurita == label && (filter == MapFilter.ALL || filter == MapFilter.FLURITA)
                        val fill = when {
                            hitLiuliu && hitFluorite -> mapOrange
                            hitLiuliu -> mapGreenSoft
                            hitFluorite -> Color(0xFFFFE1C4)
                            else -> mapPaper
                        }
                        val rectSize = Size(s(w * 0.14f), s(h * 0.12f))
                        val origin = Offset(ox(left), oy(top))
                        drawRoundRect(fill, origin, rectSize, CornerRadius(16f, 16f))
                        drawRoundRect(mapInk, origin, rectSize, CornerRadius(16f, 16f), style = Stroke(2f))
                        val measured = textMeasurer.measure(label, labelStyle)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = label,
                            topLeft = Offset(
                                origin.x + (rectSize.width - measured.size.width) / 2f,
                                origin.y + (rectSize.height - measured.size.height) / 2f,
                            ),
                            style = labelStyle,
                        )
                    }
                }
            }
        }
        PixelButton(
            text = if (scale > MIN_SCALE || panX != 0f || panY != 0f) "Centrar mapa" else "Mapa centrado",
            onClick = {
                scale = MIN_SCALE
                panX = 0f
                panY = 0f
            },
            outlined = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Pellizca para ampliar. El bosque está arriba a la derecha y las ruinas abajo a la izquierda.", style = MaterialTheme.typography.bodySmall)
        if (filter == MapFilter.FEEDERS) {
            PixelCard {
                Text("Comederos", style = MaterialTheme.typography.titleMedium)
                Text("Este dibujo no marca un punto fijo para los comederos.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            PixelCard {
                Text("Hoy", style = MaterialTheme.typography.titleMedium)
                if (filter != MapFilter.FLURITA) Text("Liuliu: ${com.guiaheartopia.app.domain.ResourceCalendar.label(spot.liuliu)}", color = Green)
                if (filter != MapFilter.LIULIU) Text("flurita: ${com.guiaheartopia.app.domain.ResourceCalendar.label(spot.flurita)}", color = Orange)
            }
        }
    }
}
