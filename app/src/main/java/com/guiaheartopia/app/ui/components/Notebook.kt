package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.R
import com.guiaheartopia.app.ui.theme.Ink

private val CardPaper = Color(0xFFFBF6E9)
private val CardLine = Color(0xFFD6C8AA)

fun watercolorFor(category: String): Int? = when (category) {
    "Peces" -> R.drawable.acuarela_peces
    "Pájaros", "Aves" -> R.drawable.acuarela_aves
    "Insectos" -> R.drawable.acuarela_insectos
    "Fauna" -> R.drawable.acuarela_fauna
    else -> null
}

fun washiFor(category: String?): Int = when (category) {
    "Peces", "Pesca" -> R.drawable.washi_teal
    "Pájaros", "Aves", "Fauna", "Cocina", "Observación de aves" -> R.drawable.washi_rosa
    "Insectos", "Siembra", "Captura de insectos" -> R.drawable.washi_verde
    else -> R.drawable.washi_kraft
}

@Composable
fun NotebookCard(
    category: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 12.dp)
                .background(CardPaper, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .border(1.dp, CardLine, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
            content = content,
        )
        Image(
            painter = painterResource(washiFor(category)),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-10).dp, y = 2.dp)
                .width(84.dp)
                .rotate(-30f),
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
fun Modifier.bleed(horizontal: Dp): Modifier = layout { measurable, constraints ->
    val extra = horizontal.roundToPx()
    val width = (constraints.maxWidth + extra * 2).coerceAtLeast(0)
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra, 0)
    }
}

@Composable
fun SectionBanner(
    image: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    bleed: Dp = 16.dp,
) {
    Box(
        modifier
            .bleed(bleed)
            .heightIn(min = 132.dp)
            .background(Color(0xFFF7EFDB)),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.42f)
                .heightIn(max = 96.dp),
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomEnd,
        )
        Column(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.78f)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFF7EFDB),
                            Color(0xF2F7EFDB),
                            Color(0x00F7EFDB),
                        ),
                    ),
                )
                .padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 16.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                softWrap = true,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2A2118),
                softWrap = true,
            )
        }
    }
}

@Composable
fun TwigDivider(index: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(if (index % 2 == 0) R.drawable.ramita_1 else R.drawable.ramita_2),
        contentDescription = "Separador",
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp),
        contentScale = ContentScale.FillWidth,
    )
}
