package com.guiaheartopia.app.ui.pixel

import android.app.Application
import android.graphics.Paint as AndroidPaint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guiaheartopia.app.pixel.ArgbImage
import com.guiaheartopia.app.pixel.BackgroundFilter
import com.guiaheartopia.app.pixel.ConvertOptions
import com.guiaheartopia.app.pixel.DefaultPalette
import com.guiaheartopia.app.pixel.FitMode
import com.guiaheartopia.app.pixel.GridSize
import com.guiaheartopia.app.pixel.GuideViewport
import com.guiaheartopia.app.pixel.ImageExporter
import com.guiaheartopia.app.pixel.ImageToPixel
import com.guiaheartopia.app.pixel.PixelBitmaps
import com.guiaheartopia.app.pixel.PixelConverter
import com.guiaheartopia.app.pixel.PixelProject
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelSwatch
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Paper
import java.io.File
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class PixelArtViewModel(app: Application) : AndroidViewModel(app) {
    var project by mutableStateOf<PixelProject?>(null)
    var message by mutableStateOf<String?>(null)
    var busy by mutableStateOf(false)
    private var source: ArgbImage? = null
    private val converter: ImageToPixel = PixelConverter
    private val json = Json { ignoreUnknownKeys = true }
    private val file = File(app.filesDir, "pixel_guia.json")

    init {
        if (file.exists()) {
            project = runCatching { json.decodeFromString(PixelProject.serializer(), file.readText()) }.getOrNull()
        }
    }

    fun load(uri: Uri) {
        busy = true
        message = null
        viewModelScope.launch {
            val image = withContext(Dispatchers.IO) {
                runCatching { PixelBitmaps.decode(getApplication(), uri) }.getOrNull()
            }
            source = image
            busy = false
            message = if (image == null) "No se pudo leer la imagen." else "Imagen lista. Ajusta la cuadrícula y convierte."
        }
    }

    fun convert(options: ConvertOptions) {
        val image = source
        if (image == null) {
            message = "Primero elige una imagen del dispositivo."
            return
        }
        busy = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                runCatching { converter.convert(image, DefaultPalette.colors, options) }.getOrNull()
            }
            busy = false
            if (result == null) {
                message = "No se pudo convertir la imagen."
            } else {
                project = result
                message = "Guía lista. Píntala a mano en el juego; esta app no lo hace por ti."
                withContext(Dispatchers.IO) {
                    runCatching { file.writeText(json.encodeToString(PixelProject.serializer(), result)) }
                }
            }
        }
    }

    fun export(guide: Boolean) {
        val current = project ?: return
        viewModelScope.launch {
            val where = withContext(Dispatchers.IO) {
                runCatching {
                    val skip = BackgroundFilter.skipMask(current, true)
                    val bitmap = if (guide) {
                        PixelBitmaps.renderGuide(current, skip)
                    } else {
                        PixelBitmaps.render(current, scale = (960 / maxOf(current.cols, current.rows)).coerceAtLeast(1), grid = false, skip = skip)
                    }
                    ImageExporter.savePng(getApplication(), bitmap, if (guide) "guia_${current.cols}x${current.rows}" else "pixel_${current.cols}x${current.rows}")
                }.getOrNull()
            }
            message = if (where == null) "No se pudo guardar la imagen." else "Imagen guardada en $where"
        }
    }

    fun share() {
        val current = project ?: return
        runCatching {
            val bitmap = PixelBitmaps.renderGuide(current, BackgroundFilter.skipMask(current, true))
            ImageExporter.sharePng(getApplication(), bitmap, "guia_pixel")
        }.onFailure { message = "No se pudo compartir." }
    }
}

@Composable
fun PixelArtScreen(initialCols: Int, initialRows: Int) {
    val vm: PixelArtViewModel = viewModel()
    val picker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) vm.load(uri)
    }
    var cols by remember { mutableIntStateOf(initialCols.coerceIn(8, 150)) }
    var rows by remember { mutableIntStateOf(initialRows.coerceIn(8, 150)) }
    var fit by remember { mutableStateOf(FitMode.CROP) }
    var maxColors by remember { mutableFloatStateOf(0f) }
    var dither by remember { mutableStateOf(false) }
    var crisp by remember { mutableStateOf(true) }
    var skipWhite by remember { mutableStateOf(true) }
    var showOptions by remember { mutableStateOf(vm.project == null) }
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Arte de píxel", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Convierte una foto en una cuadrícula para copiarla a mano. La app no pinta el juego.",
            style = MaterialTheme.typography.bodySmall,
        )
        vm.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        val project = vm.project
        if (project != null) {
            PixelViewer(project, Modifier.weight(1f).fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton("Guardar imagen", onClick = { vm.export(false) })
                PixelButton("Guardar guía", onClick = { vm.export(true) })
            }
            PixelButton("Compartir guía", onClick = { vm.share() }, container = com.guiaheartopia.app.ui.theme.Sky)
        }
        PixelButton(if (showOptions) "Ocultar ajustes" else "Ajustes de conversión", onClick = { showOptions = !showOptions })
        if (showOptions || project == null) {
            Column(
                Modifier
                    .weight(if (project == null) 1f else 0.8f, fill = project == null)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Tamaño ${cols}×$rows", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GridSize.ALL.take(8).forEach { size ->
                        FilterChip(
                            selected = cols == size.cols && rows == size.rows,
                            onClick = { cols = size.cols; rows = size.rows },
                            label = { Text("${size.cols}×${size.rows}") },
                        )
                    }
                }
                Text("Columnas $cols")
                Slider(cols.toFloat(), { cols = it.toInt().coerceIn(8, 150) }, valueRange = 8f..150f)
                Text("Filas $rows")
                Slider(rows.toFloat(), { rows = it.toInt().coerceIn(8, 150) }, valueRange = 8f..150f)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FitMode.entries.forEach { mode ->
                        FilterChip(selected = fit == mode, onClick = { fit = mode }, label = { Text(mode.label) })
                    }
                }
                Text(if (maxColors.toInt() == 0) "Colores: sin límite de la paleta" else "Colores: ${maxColors.toInt()}")
                Slider(maxColors, { maxColors = it }, valueRange = 0f..32f, steps = 31)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Contornos nítidos", Modifier.weight(1f))
                    Switch(checked = crisp && !dither, onCheckedChange = { crisp = it }, enabled = !dither)
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Tramado", Modifier.weight(1f))
                    Switch(checked = dither, onCheckedChange = { dither = it })
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Fondo blanco del borde vacío", Modifier.weight(1f))
                    Switch(checked = skipWhite, onCheckedChange = { skipWhite = it })
                }
                PixelButton("Elegir imagen", onClick = {
                    picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                })
                PixelButton(
                    if (vm.busy) "Convirtiendo…" else "Convertir",
                    onClick = {
                        if (!vm.busy) {
                            vm.convert(
                                ConvertOptions(
                                    cols = cols,
                                    rows = rows,
                                    dithering = dither,
                                    maxColors = maxColors.toInt(),
                                    fitMode = fit,
                                    crispLines = crisp,
                                    skipWhiteBackground = skipWhite,
                                    label = "${cols}×$rows",
                                ),
                            )
                        }
                    },
                )
                Text(
                    "La imagen no sale del teléfono. En Android 10 o más nuevo se guarda en Imágenes/GuiaHeartopia.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun PixelViewer(project: PixelProject, modifier: Modifier = Modifier) {
    val skip = remember(project) { BackgroundFilter.skipMask(project, skipBackground = true) }
    val image = remember(project) {
        PixelBitmaps.render(project, scale = 1, grid = false, skip = skip, checker = true).asImageBitmap()
    }
    var scale by remember(project) { mutableFloatStateOf(1f) }
    var offset by remember(project) { mutableStateOf(Offset.Zero) }
    var view by remember { mutableStateOf(IntSize.Zero) }
    var cell by remember(project) { mutableStateOf<Pair<Int, Int>?>(null) }
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .border(3.dp, Ink)
                .background(Paper)
                .onSizeChanged { view = it }
                .pointerInput(project) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val next = (scale * zoom).coerceIn(0.6f, 40f)
                        offset = (offset - centroid) * (next / scale) + centroid + pan
                        scale = next
                    }
                }
                .pointerInput(project, scale, offset, view) {
                    detectTapGestures { pos ->
                        cell = GuideViewport.cellAt(
                            view.width.toFloat(),
                            view.height.toFloat(),
                            project.cols,
                            project.rows,
                            scale,
                            offset.x,
                            offset.y,
                            pos.x,
                            pos.y,
                        )
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val base = min(size.width / project.cols, size.height / project.rows)
                val ox = (size.width - project.cols * base) / 2f
                val oy = (size.height - project.rows * base) / 2f
                val cellPx = base * scale
                val left = offset.x + ox * scale
                val top = offset.y + oy * scale
                drawImage(
                    image,
                    dstOffset = IntOffset(left.toInt(), top.toInt()),
                    dstSize = IntSize((project.cols * cellPx).toInt().coerceAtLeast(1), (project.rows * cellPx).toInt().coerceAtLeast(1)),
                    filterQuality = FilterQuality.None,
                )
                if (cellPx >= 6f) {
                    val x0 = ((-left) / cellPx).toInt().coerceIn(0, project.cols)
                    val x1 = ((size.width - left) / cellPx + 1).toInt().coerceIn(0, project.cols)
                    val y0 = ((-top) / cellPx).toInt().coerceIn(0, project.rows)
                    val y1 = ((size.height - top) / cellPx + 1).toInt().coerceIn(0, project.rows)
                    for (x in x0..x1) {
                        drawLine(Color(0x66000000), Offset(left + x * cellPx, top + y0 * cellPx), Offset(left + x * cellPx, top + y1 * cellPx), if (x % 10 == 0) 2f else 1f)
                    }
                    for (y in y0..y1) {
                        drawLine(Color(0x66000000), Offset(left + x0 * cellPx, top + y * cellPx), Offset(left + x1 * cellPx, top + y * cellPx), if (y % 10 == 0) 2f else 1f)
                    }
                    if (cellPx >= 22f) {
                        val text = AndroidPaint().apply {
                            textAlign = AndroidPaint.Align.CENTER
                            textSize = cellPx * 0.38f
                            color = 0xFF3A271C.toInt()
                        }
                        val native = drawContext.canvas.nativeCanvas
                        for (y in y0 until y1) {
                            for (x in x0 until x1) {
                                val v = project.colorAt(x, y)
                                if (v < 0 || skip[project.index(x, y)]) continue
                                native.drawText("${v + 1}", left + (x + 0.5f) * cellPx, top + (y + 0.72f) * cellPx, text)
                            }
                        }
                    }
                }
                drawRect(Ink, Offset(left, top), Size(project.cols * cellPx, project.rows * cellPx), style = Stroke(2f))
            }
        }
        val picked = cell
        if (picked == null) {
            Text("Pellizca para ampliar. Toca una casilla para ver columna, fila y color. Los números aparecen cuando la casilla es legible.", style = MaterialTheme.typography.bodySmall)
        } else {
            val (x, y) = picked
            val v = project.colorAt(x, y)
            Text("Columna ${x + 1} · fila ${y + 1}", style = MaterialTheme.typography.titleSmall)
            if (v < 0 || skip[project.index(x, y)]) {
                Text("Casilla vacía en la guía.", style = MaterialTheme.typography.bodyMedium)
            } else {
                val color = project.colors[v]
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelSwatch(Color(color.argb))
                    Text("${v + 1}. ${color.name} ${color.hex}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            project.colors.forEachIndexed { index, color ->
                Column {
                    PixelSwatch(Color(color.argb))
                    Text("${index + 1}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
