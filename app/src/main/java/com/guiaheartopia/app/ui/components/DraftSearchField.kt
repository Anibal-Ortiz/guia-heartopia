package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.ui.theme.Muted

/**
 * Buscador simple compartido. El `value` se mantiene síncrono en memoria: quien llama
 * actualiza el estado en el mismo `onValueChange`, sin esperar a un Flow/IO antes de
 * reescribir el texto (evita que el cursor salte al principio).
 */
@Composable
fun DraftSearchField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    showClear: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        label = label?.let { { Text(it) } },
        singleLine = true,
        trailingIcon = if (showClear && value.text.isNotEmpty()) {
            {
                Text(
                    "✕",
                    modifier = Modifier
                        .clickable { onValueChange(TextFieldValue("")) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Limpiar búsqueda"
                        },
                    color = Muted,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        } else {
            null
        },
    )
}
