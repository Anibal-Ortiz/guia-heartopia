package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.guiaheartopia.app.data.AndroidContentStore
import com.guiaheartopia.app.domain.AficionesImagePaths
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.artworkColorFilter
@Composable
fun rememberAficionesModel(imagen: String?): Any? {
    val context = LocalContext.current
    return remember(imagen, context) {
        AficionesImagePaths.candidates(imagen).firstNotNullOfOrNull { path ->
            AndroidContentStore.overlayFile(context, path)
                ?: if (assetExists(context, path)) "file:///android_asset/$path" else null
        }
    }
}

private fun assetExists(context: android.content.Context, path: String): Boolean =
    runCatching { context.assets.open(path).close() }.isSuccess

@Composable
fun AficionesImage(
    imagen: String?,
    sectionLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    shapeRadius: Dp = 16.dp,
    contentDescription: String = "Dibujo",
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(shapeRadius)
    val model = rememberAficionesModel(imagen)
    var failed by remember(model) { mutableStateOf(false) }
    val stain = watercolorFor(sectionLabel)
    val artworkFilter = artworkColorFilter()
    val clickable = if (onClick == null) {
        Modifier
    } else {
        Modifier
            .semantics { this.contentDescription = contentDescription }
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
                colorFilter = artworkFilter,
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
                    model = ImageRequest.Builder(LocalContext.current).data(model).build(),
                    contentDescription = if (onClick == null) contentDescription else null,
                    contentScale = ContentScale.Fit,
                    colorFilter = artworkFilter,
                    onError = { failed = true },
                    modifier = Modifier.fillMaxSize().clip(shape),
                )
            } else {
                if (sectionLabel == "Ingrediente") {
                    Image(
                        painter = painterResource(com.guiaheartopia.app.R.drawable.ic_aficion_cocina),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    CreatureGlyph(
                        category = sectionLabel,
                        locations = emptyList(),
                        level = null,
                        modifier = Modifier.fillMaxSize(),
                        size = size,
                    )
                }
            }
        }
    }
}

@Composable
fun IngredientImage(
    imagen: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    contentDescription: String = "Ingrediente",
) {
    AficionesImage(
        imagen = imagen,
        sectionLabel = "Ingrediente",
        modifier = modifier,
        size = size,
        shapeRadius = 10.dp,
        contentDescription = contentDescription,
    )
}