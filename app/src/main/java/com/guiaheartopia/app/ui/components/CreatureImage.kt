package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription as semanticsDescription
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
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
@Composable
fun rememberCreatureModel(imageUrl: String?, creatureId: String): Any? {
    val context = LocalContext.current
    val downloaded = CreatureImagePaths.candidates(imageUrl, creatureId)
        .firstNotNullOfOrNull { AndroidContentStore.overlayFile(context, it) }
    return bundledCreatureModel(imageUrl, downloaded, creatureId)
}

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
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(shapeRadius)
    val context = LocalContext.current
    val downloaded = CreatureImagePaths.candidates(imageUrl, creatureId)
        .firstNotNullOfOrNull { AndroidContentStore.overlayFile(context, it) }
    val model = bundledCreatureModel(imageUrl, downloaded, creatureId)
    var failed by remember(model) { mutableStateOf(false) }
    val stain = watercolorFor(category)

    val clickable = if (onClick == null) {
        Modifier
    } else {
        Modifier
            .semantics { semanticsDescription = contentDescription }
            .clickable(onClick = onClick)
    }
    Box(
        modifier.wrapContentSize(unbounded = true).then(clickable),
        contentAlignment = Alignment.Center,
    ) {
        if (stain != null) {
            Image(
                painter = painterResource(stain),
                contentDescription = null,
                modifier = Modifier
                    .size(size * 1.3f)
                    .graphicsLayer { alpha = 0.85f },
                contentScale = ContentScale.Fit,
            )
        }
        Box(
            Modifier
                .size(size)
                .clip(shape)
                .border(1.dp, Line.copy(alpha = 0.5f), shape),
            contentAlignment = Alignment.Center,
        ) {
            if (model != null && !failed) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(model)
                        .build(),
                    contentDescription = if (onClick == null) contentDescription else null,
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
}
