package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.R
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Ochre
import com.guiaheartopia.app.ui.theme.Paper

@Composable
fun ConseguidoMarkIcon(on: Boolean, modifier: Modifier = Modifier, size: Dp = 26.dp) {
    Image(
        painter = painterResource(if (on) R.drawable.ic_marca_conseguido_si else R.drawable.ic_marca_conseguido_no),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}

@Composable
fun CertificadoMarkIcon(on: Boolean, modifier: Modifier = Modifier, size: Dp = 26.dp) {
    Image(
        painter = painterResource(if (on) R.drawable.ic_marca_certificado_si else R.drawable.ic_marca_certificado_no),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}

@Composable
fun NoCertificableMarkIcon(modifier: Modifier = Modifier, size: Dp = 26.dp) {
    Image(
        painter = painterResource(R.drawable.ic_marca_no_certificable),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}

@Composable
fun MarkIconButton(
    on: Boolean,
    description: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val clickable = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (on) HoneySoft else Paper)
            .border(width = if (on) 2.dp else 1.dp, color = if (on) Ochre else Line, shape = CircleShape)
            .then(clickable)
            .clearAndSetSemantics {
                contentDescription = description
                if (onClick != null) role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}