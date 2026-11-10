package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.guiaheartopia.app.data.AndroidContentStore
import com.guiaheartopia.app.domain.CreatureImagePaths
import com.guiaheartopia.app.domain.CreatureMark
import com.guiaheartopia.app.ui.theme.Line
import java.io.File

/**
 * Solo muestra una imagen si el paquete remoto ya la guardó en el teléfono.
 * El APK no trae arte de catálogo: si no hay archivo, la ficha usa el ícono de la categoría.
 * Una URL http(s) no se pide al mostrar la ficha.
 */
fun bundledCreatureModel(imageUrl: String?, downloaded: File? = null, id: String = ""): Any? {
    val paths = CreatureImagePaths.candidates(imageUrl, id)
    if (paths.isEmpty()) return null
    if (downloaded != null && downloaded.isFile && downloaded.length() > 0L) return downloaded
    return null
}

@Composable
fun CreatureImage(
    imageUrl: String?,
    category: String,
    locations: List<String>,
    level: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    shapeRadius: Dp = 16.dp,
    contentDescription: String = "Dibujo de la criatura",
    creatureId: String = "",
) {
    val habitat = CreatureMark.habitat(locations, level)
    val tint = habitatColor(habitat, level)
    val shape = RoundedCornerShape(shapeRadius)
    val context = LocalContext.current
    val downloaded = CreatureImagePaths.candidates(imageUrl, creatureId)
        .firstNotNullOfOrNull { AndroidContentStore.overlayFile(context, it) }
    val model = bundledCreatureModel(imageUrl, downloaded, creatureId)
    var failed by remember(model) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(tint.copy(alpha = 0.14f))
            .border(1.dp, Line.copy(alpha = 0.5f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (model != null && !failed) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(model)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                onError = { failed = true },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
            )
        } else {
            CreatureGlyph(
                category = category,
                locations = locations,
                level = level,
                modifier = Modifier.fillMaxSize(),
                size = size,
            )
        }
    }
}
