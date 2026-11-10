package com.guiaheartopia.app.domain

enum class ContentSyncResult {
    APPLIED,
    UNCHANGED,
    OLDER,
    NEEDS_NEWER_APP,
    NETWORK_ERROR,
    VERIFY_FAILED,
}

data class ContentSyncReport(
    val result: ContentSyncResult,
    val localVersion: Int,
    val remoteVersion: Int? = null,
)

object ContentSyncMessages {
    fun message(report: ContentSyncReport): String = when (report.result) {
        ContentSyncResult.APPLIED -> {
            val shown = report.remoteVersion ?: report.localVersion
            "Contenido actualizado a la versión $shown."
        }
        ContentSyncResult.UNCHANGED,
        ContentSyncResult.OLDER,
        -> "Tu contenido está al día (versión ${report.localVersion})."
        ContentSyncResult.NEEDS_NEWER_APP -> "Hay contenido nuevo, pero requiere actualizar la app."
        ContentSyncResult.NETWORK_ERROR -> "No se pudo conectar. Revisa tu internet e inténtalo de nuevo."
        ContentSyncResult.VERIFY_FAILED -> "La descarga falló la verificación; se mantiene el contenido anterior."
    }
}
