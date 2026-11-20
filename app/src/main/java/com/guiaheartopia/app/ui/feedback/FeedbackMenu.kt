package com.guiaheartopia.app.ui.feedback

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.ui.components.CozyChatIcon
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.PaperSubtle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val FORM_ID = "1FAIpQLScc0khs_rNd1yKdc9wEX00bz2KeNVZ9IPdcXWgICfV2bdtaDw"
private const val ENTRY_EMAIL = "1614510342"
private const val ENTRY_MESSAGE = "1995474196"
private const val FEEDBACK_URL = "https://docs.google.com/forms/d/e/$FORM_ID/formResponse"
private const val MAX_MESSAGE = 2_000
private const val MAX_ATTEMPTS = 3

/** Botón pequeño que abre directamente el cuadro para escribir al desarrollador. */
@Composable
fun FeedbackMenu() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var replyEmail by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .testTag("open_feedback")
            .size(38.dp)
            .clip(CircleShape)
            .background(PaperSubtle)
            .border(1.dp, Line, CircleShape)
            .clickable { showDialog = true }
            .semantics { contentDescription = "Comentarios y preguntas" },
        contentAlignment = Alignment.Center,
    ) {
        CozyChatIcon(tint = Ink, size = 18.dp)
    }

    if (showDialog) {
        val canSend = message.isNotBlank() &&
            message.length <= MAX_MESSAGE &&
            (replyEmail.isBlank() || replyEmail.trim().contains("@"))
        AlertDialog(
            onDismissRequest = {
                if (!sending) {
                    showDialog = false
                    message = ""
                    replyEmail = ""
                }
            },
            title = { Text("Escribe al desarrollador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Cuéntanos lo que quieras: comentarios, preguntas o aclaraciones. Se envía desde la app, sin abrir el correo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Tu mensaje") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 8,
                        enabled = !sending,
                    )
                    OutlinedTextField(
                        value = replyEmail,
                        onValueChange = { replyEmail = it },
                        label = { Text("Tu correo (opcional, para responderte)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !sending,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sending = true
                        scope.launch {
                            val error = withContext(Dispatchers.IO) {
                                sendFeedback(message, replyEmail)
                            }
                            sending = false
                            Toast.makeText(
                                context,
                                if (error == null) {
                                    "Mensaje enviado. ¡Gracias!"
                                } else {
                                    "No se pudo enviar: $error"
                                },
                                Toast.LENGTH_LONG,
                            ).show()
                            if (error == null) {
                                showDialog = false
                                message = ""
                                replyEmail = ""
                            }
                        }
                    },
                    enabled = canSend && !sending,
                ) {
                    Text(if (sending) "Enviando…" else "Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        message = ""
                        replyEmail = ""
                    },
                    enabled = !sending,
                ) {
                    Text("Cancelar")
                }
            },
        )
    }
}

/**
 * Envía el mensaje directamente al Google Forms del desarrollador. Devuelve null si se aceptó
 * o un texto con el motivo del fallo para poder diagnosticarlo desde la app.
 */
private fun sendFeedback(message: String, replyEmail: String): String? {
    val fields = LinkedHashMap<String, String>()
    fields["entry.$ENTRY_MESSAGE"] = message.trim().take(MAX_MESSAGE)
    if (replyEmail.isNotBlank()) {
        fields["entry.$ENTRY_EMAIL"] = replyEmail.trim()
    }
    val body = fields.entries.joinToString("&") { (key, value) ->
        URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8")
    }.toByteArray(Charsets.UTF_8)

    var lastError = "error desconocido"
    repeat(MAX_ATTEMPTS) { attempt ->
        val connection = (URL(FEEDBACK_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 20_000
            doOutput = true
            useCaches = false
            instanceFollowRedirects = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            setRequestProperty("Accept", "text/html,application/xhtml+xml")
            setRequestProperty("Accept-Language", "es-ES,es;q=0.9")
        }
        try {
            connection.outputStream.use { it.write(body) }
            val code = connection.responseCode
            if (code in 200..299) {
                Log.d("Feedback", "Enviado en el intento ${attempt + 1} (HTTP $code)")
                return null
            }
            lastError = "HTTP $code"
            Log.w("Feedback", "Intento ${attempt + 1}: HTTP $code")
        } catch (e: Exception) {
            lastError = e.javaClass.simpleName + (e.message?.let { ": $it" } ?: "")
            Log.w("Feedback", "Intento ${attempt + 1} fallido", e)
        } finally {
            connection.disconnect()
        }
    }
    return lastError
}
