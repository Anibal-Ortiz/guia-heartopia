package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.guiaheartopia.app.domain.CreatureMark
import com.guiaheartopia.app.ui.theme.Line

@Composable
fun CreatureImage(
    imageUrl: String?,
    category: String,
    locations: List<String>,
    level: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    shapeRadius: Dp = 16.dp,
) {
    val habitat = CreatureMark.habitat(locations, level)
    val tint = habitatColor(habitat, level)
    val shape = RoundedCornerShape(shapeRadius)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(tint.copy(alpha = 0.14f))
            .border(1.dp, Line.copy(alpha = 0.5f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
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
