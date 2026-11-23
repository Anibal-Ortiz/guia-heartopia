package com.guiaheartopia.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guiaheartopia.app.data.LicensePreferences
import com.guiaheartopia.app.data.LicenseState
import com.guiaheartopia.app.data.PolarLicenseRepository
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import kotlinx.coroutines.launch

@Composable
fun AccessCodeSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preferences = remember(context) { LicensePreferences.get(context) }
    val repository = remember(context) { PolarLicenseRepository.get(context) }
    val state by preferences.stateFlow.collectAsStateWithLifecycle(LicenseState())
    val scope = rememberCoroutineScope()

    var code by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmUnlink by remember { mutableStateOf(false) }

    fun activate() {
        if (busy || code.isBlank()) return
        busy = true
        error = null
        scope.launch {
            try {
                repository.activateLicense(code)
                    .onSuccess { code = "" }
                    .onFailure {
                        error = it.message ?: "No se pudo activar el código."
                    }
            } finally {
                busy = false
            }
        }
    }

    fun unlink() {
        confirmUnlink = false
        busy = true
        error = null
        scope.launch {
            try {
                repository.deactivateCurrentDevice()
                    .onFailure {
                        error = it.message ?: "No se pudo desvincular este dispositivo."
                    }
            } finally {
                busy = false
            }
        }
    }

    PixelCard(modifier = modifier) {
        Text("Código de acceso", style = MaterialTheme.typography.titleMedium)

        if (!state.loaded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("Comprobando…", style = MaterialTheme.typography.bodySmall)
            }
            return@PixelCard
        }

        if (state.isPro) {
            Surface(
                color = GreenSoft,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    "Acceso activo — Anuncios desactivados",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = GreenDark,
                )
            }
            Text(
                state.maskedKey,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            )
            TextButton(
                onClick = { confirmUnlink = true },
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("access_code_deactivate"),
            ) {
                Text("Desvincular este dispositivo")
            }
            if (busy) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Desvinculando…", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            Text(
                "Si tienes un código de acceso, ingrésalo aquí para desactivar los anuncios.",
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it.uppercase()
                    if (error != null) error = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("access_code_field"),
                label = { Text("Código") },
                singleLine = true,
                isError = error != null,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { activate() }),
            )
            Button(
                onClick = { activate() },
                enabled = !busy && code.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("access_code_activate"),
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Activando…")
                } else {
                    Text("Activar")
                }
            }
        }

        error?.let { message ->
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    if (confirmUnlink) {
        AlertDialog(
            onDismissRequest = { if (!busy) confirmUnlink = false },
            title = { Text("Desvincular este dispositivo") },
            text = {
                Text(
                    "Si desvinculas este dispositivo volverán a mostrarse anuncios " +
                        "y liberarás el cupo para usarlo en otro equipo.",
                )
            },
            confirmButton = {
                TextButton(onClick = { unlink() }, enabled = !busy) {
                    Text("Desvincular")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmUnlink = false }, enabled = !busy) {
                    Text("Cancelar")
                }
            },
        )
    }
}
