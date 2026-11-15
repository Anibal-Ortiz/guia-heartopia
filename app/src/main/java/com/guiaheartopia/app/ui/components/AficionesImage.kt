package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.domain.AficionesImagePaths
import com.guiaheartopia.app.ui.theme.Line
@Composable
fun rememberAficionesModel(imagen: String?): Any? {
    val context = LocalContext.current
    val library = remember { AndroidContentLibrary(context) }
    return AficionesImagePaths.candidates(imagen)
        .firstNotNullOfOrNull { library.effectiveBytes(it) }
        ?.takeIf { it.isNotEmpty() }
}

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
                    onError = { failed = true },
                    modifier = Modifier.fillMaxSize().clip(shape),
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