package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.ui.theme.Beige
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Paper

@Composable
fun PixelCard(
    modifier: Modifier = Modifier,
    container: Color = Paper,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(RoundedCornerShape(22.dp))
            .background(container)
            .border(1.dp, Line, RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Green,
    contentColor: Color = if (container == Paper) Green else Color.White,
    outlined: Boolean = false,
) {
    val shape = RoundedCornerShape(999.dp)
    val bg = if (outlined) Paper else container
    val fg = if (outlined) Green else contentColor
    Text(
        text,
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(if (outlined) 1.5.dp else 0.dp, if (outlined) Line else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        style = MaterialTheme.typography.labelLarge,
        color = fg,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun VerifyBadge(text: String, modifier: Modifier = Modifier, tone: Color = com.guiaheartopia.app.ui.theme.Yellow) {
    Text(
        text,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tone)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = if (tone == GreenSoft) Green else Ink,
    )
}

@Composable
fun NowBadge(text: String = "Ahora") {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(GreenSoft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = Green,
    )
}

@Composable
fun LabelValue(label: String, value: String, emphasize: Boolean = true) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.weight(0.9f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (emphasize) Green else Ink,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f),
        )
    }
}

@Composable
fun PillTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            val on = index == selected
            Text(
                label,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (on) Green else Beige)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = if (on) Color.White else Ink,
            )
        }
    }
}

@Composable
fun PixelProgress(fraction: Float, modifier: Modifier = Modifier, color: Color = Green, track: Color = Beige) {
    LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp)),
        color = color,
        trackColor = track,
        drawStopIndicator = {},
    )
}

enum class NavIcon { HOME, ROUTINE, GUIDE, PIXEL, HOBBIES }

data class NavItem(val route: String, val label: String, val icon: NavIcon)

@Composable
fun PixelNavBar(
    items: List<NavItem>,
    current: String?,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Paper)
            .padding(vertical = 6.dp),
    ) {
        items.forEach { item ->
            val selected = current == item.route
            val tint = if (selected) Green else Muted
            Column(
                Modifier
                    .weight(1f)
                    .semantics {
                        role = Role.Tab
                        this.selected = selected
                    }
                    .clickable { onSelect(item.route) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LineNavIcon(item.icon, tint)
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun LineNavIcon(icon: NavIcon, tint: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6.dp.toPx())
        when (icon) {
            NavIcon.HOME -> {
                drawPath(
                    androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0.16f)
                        lineTo(size.width * 0.86f, size.height * 0.46f)
                        lineTo(size.width * 0.74f, size.height * 0.46f)
                        lineTo(size.width * 0.74f, size.height * 0.84f)
                        lineTo(size.width * 0.26f, size.height * 0.84f)
                        lineTo(size.width * 0.26f, size.height * 0.46f)
                        lineTo(size.width * 0.14f, size.height * 0.46f)
                        close()
                    },
                    tint,
                    style = stroke,
                )
            }
            NavIcon.ROUTINE -> {
                drawRoundRect(tint, androidx.compose.ui.geometry.Offset(size.width * 0.22f, size.height * 0.16f), androidx.compose.ui.geometry.Size(size.width * 0.56f, size.height * 0.7f), androidx.compose.ui.geometry.CornerRadius(4f, 4f), style = stroke)
                drawLine(tint, androidx.compose.ui.geometry.Offset(size.width * 0.34f, size.height * 0.4f), androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.4f), strokeWidth = 1.6.dp.toPx())
                drawLine(tint, androidx.compose.ui.geometry.Offset(size.width * 0.34f, size.height * 0.55f), androidx.compose.ui.geometry.Offset(size.width * 0.58f, size.height * 0.55f), strokeWidth = 1.6.dp.toPx())
            }
            NavIcon.GUIDE -> {
                drawRoundRect(tint, androidx.compose.ui.geometry.Offset(size.width * 0.22f, size.height * 0.18f), androidx.compose.ui.geometry.Size(size.width * 0.56f, size.height * 0.66f), androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = stroke)
                drawLine(tint, androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.18f), androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.84f), strokeWidth = 1.6.dp.toPx())
            }
            NavIcon.PIXEL -> {
                val cell = size.minDimension / 5f
                for (y in 1..3) for (x in 1..3) {
                    drawRect(tint, androidx.compose.ui.geometry.Offset(x * cell, y * cell), androidx.compose.ui.geometry.Size(cell * 0.72f, cell * 0.72f), style = stroke)
                }
            }
            NavIcon.HOBBIES -> {
                drawCircle(tint, size.minDimension * 0.22f, center, style = stroke)
                drawCircle(tint, size.minDimension * 0.08f, center)
            }
        }
    }
}

@Composable
fun SoftCheck(checked: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) Green else Paper)
            .border(1.5.dp, if (checked) Green else Line, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Text("✓", color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun StepBadge() {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Green),
        contentAlignment = Alignment.Center,
    ) {
        Text("G", color = Color.White, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun PixelSwatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(1.dp, Line, RoundedCornerShape(6.dp)),
    )
}
