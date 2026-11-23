package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.guiaheartopia.app.domain.ImageZoom
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.artworkColorFilter

@Composable
fun CreatureZoomDialog(
    name: String,
    model: Any?,
    category: String,
    locations: List<String>,
    level: Int?,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier
                .testTag("zoom-dialog")
                .fillMaxSize()
                .background(Color(0xCC1C140C))
                .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .pointerInput(Unit) { detectTapGestures { } },
                shape = RoundedCornerShape(18.dp),
                color = Paper,
            ) {
                Column(
                    Modifier
                        .border(1.dp, Line, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ZoomableModel(
                        model = model,
                        category = category,
                        locations = locations,
                        level = level,
                        contentDescription = name,
                    )
                    Text(
                        name,
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }
            }
        }
    }
}

@Composable
private fun ZoomableModel(
    model: Any?,
    category: String,
    locations: List<String>,
    level: Int?,
    contentDescription: String,
) {
    var scale by remember { mutableFloatStateOf(ImageZoom.MIN) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(Size.Zero) }
    // Los gestos se registran una sola vez: rememberUpdatedState evita leer valores viejos.
    val gestureState = rememberUpdatedState(Triple(scale, offset, boxSize))

    fun clampOffset(value: Offset, currentScale: Float, size: Size): Offset {
        if (currentScale <= ImageZoom.MIN || size == Size.Zero) return Offset.Zero
        val maxX = (currentScale - 1f) * size.width / 2f
        val maxY = (currentScale - 1f) * size.height / 2f
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }

    Box(
        Modifier
            .sizeIn(minWidth = 220.dp, minHeight = 220.dp, maxWidth = 360.dp, maxHeight = 360.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .onSizeChanged { size ->
                val next = Size(size.width.toFloat(), size.height.toFloat())
                if (next != boxSize) {
                    boxSize = next
                    offset = clampOffset(offset, scale, next)
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val (currentScale, currentOffset, size) = gestureState.value
                    val nextScale = ImageZoom.pinch(currentScale, zoom)
                    scale = nextScale
                    offset = if (nextScale <= ImageZoom.MIN) {
                        Offset.Zero
                    } else {
                        clampOffset(currentOffset + pan, nextScale, size)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        val (currentScale, currentOffset, size) = gestureState.value
                        val nextScale = ImageZoom.doubleTap(currentScale)
                        scale = nextScale
                        offset = if (nextScale <= ImageZoom.MIN) {
                            Offset.Zero
                        } else {
                            clampOffset(currentOffset, nextScale, size)
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(model).build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                colorFilter = artworkColorFilter(),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
        } else {
            CreatureGlyph(
                category = category,
                locations = locations,
                level = level,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                size = 220.dp,
            )
        }
    }
}
