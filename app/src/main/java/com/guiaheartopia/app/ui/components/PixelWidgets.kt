package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.ui.theme.Coral
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.Peach
import com.guiaheartopia.app.ui.theme.Sage

@Composable
fun PixelCard(
    modifier: Modifier = Modifier,
    container: Color = Paper,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.padding(end = 4.dp, bottom = 4.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(4.dp, 4.dp)
                .background(Ink),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .border(3.dp, Ink)
                .background(container)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Coral,
) {
    Box(modifier.padding(end = 3.dp, bottom = 3.dp).clickable(onClick = onClick)) {
        Box(Modifier.matchParentSize().offset(3.dp, 3.dp).background(Ink))
        Text(
            text,
            modifier = Modifier
                .border(3.dp, Ink)
                .background(container)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Ink,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun VerifyBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier
            .border(2.dp, Ink)
            .background(Honey)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
fun PixelProgress(fraction: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Ink)
            .clip(RectangleShape),
        color = Sage,
        trackColor = Peach,
        drawStopIndicator = {},
    )
}

data class NavItem(val route: String, val label: String, val glyph: String)

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
            .border(3.dp, Ink)
            .background(Paper),
    ) {
        items.forEach { item ->
            val selected = current == item.route
            Column(
                Modifier
                    .weight(1f)
                    .semantics {
                        role = Role.Tab
                        this.selected = selected
                    }
                    .clickable { onSelect(item.route) }
                    .background(if (selected) Peach else Paper)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(item.glyph, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun PixelSwatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(22.dp)
            .border(2.dp, Ink)
            .background(color),
    )
}
