package com.guiaheartopia.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.ui.theme.Dark
import com.guiaheartopia.app.ui.theme.DarkCard
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GreenTint
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.PaperSubtle

@Composable
fun PixelCard(
    modifier: Modifier = Modifier,
    container: Color = Paper,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    elevation: Dp = 0.dp,
    borderColor: Color = Color(0xFFD6C8AA),
    category: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NotebookCard(category = category, modifier = modifier, content = content)
}

@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Green,
    contentColor: Color = if (container == Paper) Green else Color.White,
    outlined: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = if (outlined) Paper else container
    val fg = if (outlined) (if (container == Paper) Green else container) else contentColor
    val borderStroke = if (outlined) 1.5.dp else 0.dp
    val borderCol = if (outlined) Line else Color.Transparent

    Row(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(borderStroke, borderCol, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = fg.copy(alpha = 0.2f)),
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun VerifyBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: Color = HoneySoft,
    textColor: Color = Color.Unspecified,
) {
    val fg = if (textColor != Color.Unspecified) textColor else if (tone == GreenSoft) Green else Ink
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tone)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("✦", fontSize = 11.sp, color = fg)
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
        )
    }
}

@Composable
fun NowBadge(text: String = "Ahora") {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(GreenSoft)
            .border(1.dp, GreenTint, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Green),
        )
        Text(
            text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = GreenDark,
        )
    }
}

@Composable
fun LabelValue(label: String, value: String, emphasize: Boolean = true) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.weight(0.9f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (emphasize) Green else Ink,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f),
        )
    }
}

@Composable
fun PillTabs(
    tabs: List<String>,
    selected: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            val on = index == selected
            val bg by animateColorAsState(if (on) Green else Paper, tween(200), label = "tabBg")
            val fg by animateColorAsState(if (on) Color.White else Ink, tween(200), label = "tabFg")
            val borderCol = if (on) Green else Line

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(bg)
                    .border(1.dp, borderCol, RoundedCornerShape(999.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                    ) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                watercolorFor(label)?.let { stain ->
                    Image(
                        painter = painterResource(stain),
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer { alpha = 0.4f },
                        contentScale = ContentScale.Crop,
                    )
                }
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = fg,
                )
            }
        }
    }
}

@Composable
fun PixelProgress(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = Green,
    track: Color = PaperSubtle,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "progress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(color),
        )
    }
}

enum class NavIcon { HOME, GUIDE, CALENDAR, HOBBIES }

data class NavItem(val route: String, val label: String, val icon: NavIcon)

@Composable
fun PixelNavBar(
    items: List<NavItem>,
    current: String?,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, ambientColor = Color(0x10000000), spotColor = Color(0x18000000)),
        color = Color.Transparent,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = Line)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = current == item.route
                val tint by animateColorAsState(if (selected) GreenDark else Muted, tween(200), label = "navTint")
                val pillBg by animateColorAsState(if (selected) GreenSoft else Color.Transparent, tween(200), label = "navBg")

                Column(
                    Modifier
                        .weight(1f)
                        .testTag("nav_${item.route}")
                        .semantics {
                            role = Role.Tab
                            this.selected = selected
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(pillBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Green.copy(alpha = 0.2f)),
                        ) { onSelect(item.route) }
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    LineNavIcon(item.icon, tint = tint, filled = selected)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                        ),
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun LineNavIcon(icon: NavIcon, tint: Color, filled: Boolean = false) {
    when (icon) {
        NavIcon.HOME -> CozyHomeIcon(tint = tint, size = 22.dp, filled = filled)
        NavIcon.GUIDE -> CozyGuideIcon(tint = tint, size = 22.dp, filled = filled)
        NavIcon.CALENDAR -> CozyCalendarIcon(tint = tint, size = 22.dp, filled = filled)
        NavIcon.HOBBIES -> CozyHobbiesIcon(tint = tint, size = 22.dp, filled = filled)
    }
}

@Composable
fun SoftCheck(
    checked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(if (checked) Green else Paper, tween(200), label = "checkBg")
    val borderCol by animateColorAsState(if (checked) Green else Line, tween(200), label = "checkBorder")
    val scale by animateFloatAsState(if (checked) 1f else 0.95f, tween(150), label = "checkScale")

    Box(
        modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(2.dp, borderCol, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Green.copy(alpha = 0.2f)),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Text(
                "✓",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
            )
        }
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
        Text("♥", color = Color.White, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun PixelSwatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(1.dp, Line, RoundedCornerShape(8.dp)),
    )
}
