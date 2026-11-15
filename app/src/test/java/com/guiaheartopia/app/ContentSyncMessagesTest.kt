package com.guiaheartopia.app

import com.guiaheartopia.app.domain.ContentSyncMessages
import com.guiaheartopia.app.domain.ContentSyncReport
import com.guiaheartopia.app.domain.ContentSyncResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentSyncMessagesTest {
    @Test
    fun upToDateMessageUsesLocalVersion() {
        val text = ContentSyncMessages.message(ContentSyncReport(ContentSyncResult.UNCHANGED, localVersion = 5))
        assertEquals("Tu contenido está al día (versión 5).", text)
    }

    @Test
    fun olderRemoteStillShowsUpToDateMessage() {
        val text = ContentSyncMessages.message(ContentSyncReport(ContentSyncResult.OLDER, localVersion = 7))
        assertEquals("Tu contenido está al día (versión 7).", text)
    }

    @Test
    fun appliedMessageShowsRemoteVersion() {
        val text = ContentSyncMessages.message(
            ContentSyncReport(ContentSyncResult.APPLIED, localVersion = 8, remoteVersion = 9),
        )
        assertEquals("Contenido actualizado a la versión 9.", text)
    }

    @Test
    fun needsNewerAppMessage() {
        val text = ContentSyncMessages.message(ContentSyncReport(ContentSyncResult.NEEDS_NEWER_APP, localVersion = 3))
        assertEquals("Hay contenido nuevo, pero requiere actualizar la app.", text)
    }

    @Test
    fun networkErrorMessage() {
        val text = ContentSyncMessages.message(ContentSyncReport(ContentSyncResult.NETWORK_ERROR, localVersion = 2))
        assertEquals("No se pudo conectar. Revisa tu internet e inténtalo de nuevo.", text)
    }

    @Test
    fun verifyFailedMessage() {
        val text = ContentSyncMessages.message(ContentSyncReport(ContentSyncResult.VERIFY_FAILED, localVersion = 4))
        assertEquals("La descarga falló la verificación; se mantiene el contenido anterior.", text)
    }
}
