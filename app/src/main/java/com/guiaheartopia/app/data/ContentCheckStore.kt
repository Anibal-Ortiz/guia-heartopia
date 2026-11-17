package com.guiaheartopia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.guiaheartopia.app.domain.ContentSyncMessages
import com.guiaheartopia.app.domain.ContentSyncReport
import com.guiaheartopia.app.domain.ContentSyncResult
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.contentCheckStore by preferencesDataStore(name = "content_checks")

data class ContentCheckState(
    val lastCheckedMillis: Long = 0L,
    val lastMessage: String = "",
    val result: ContentSyncResult? = null,
)

object ContentCheckStore {
    private val KEY_CHECKED_MS = longPreferencesKey("last_checked_ms")
    private val KEY_RESULT = stringPreferencesKey("last_result")
    private val KEY_LOCAL_VERSION = intPreferencesKey("last_local_version")
    private val KEY_REMOTE_VERSION = intPreferencesKey("last_remote_version")
    private val chile: ZoneId = ZoneId.of("America/Santiago")
    private val clock: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun observe(context: Context): Flow<ContentCheckState> =
        context.applicationContext.contentCheckStore.data.map { prefs ->
            val result = prefs[KEY_RESULT]?.let { runCatching { ContentSyncResult.valueOf(it) }.getOrNull() }
            val local = prefs[KEY_LOCAL_VERSION] ?: ContentPackageFallback.localVersion(context)
            val remote = prefs[KEY_REMOTE_VERSION]?.takeIf { it >= 0 }
            val message = if (result != null) {
                ContentSyncMessages.message(ContentSyncReport(result, local, remote))
            } else {
                ""
            }
            ContentCheckState(
                lastCheckedMillis = prefs[KEY_CHECKED_MS] ?: 0L,
                lastMessage = message,
                result = result,
            )
        }

    suspend fun record(context: Context, report: ContentSyncReport, checkedAtMillis: Long) {
        context.applicationContext.contentCheckStore.edit { prefs ->
            prefs[KEY_CHECKED_MS] = checkedAtMillis
            prefs[KEY_RESULT] = report.result.name
            prefs[KEY_LOCAL_VERSION] = report.localVersion
            prefs[KEY_REMOTE_VERSION] = report.remoteVersion ?: -1
        }
        if (report.result != ContentSyncResult.NETWORK_ERROR) {
            AndroidContentStore.markAttempt(context, checkedAtMillis)
        }
    }

    fun lastCheckedLabel(epochMillis: Long): String {
        if (epochMillis <= 0L) return "Sin comprobaciones aún"
        val text = Instant.ofEpochMilli(epochMillis).atZone(chile).format(clock)
        return "Última comprobación: $text"
    }
}

private object ContentPackageFallback {
    fun localVersion(context: Context): Int = AndroidContentStore.status(context).contentVersion
}
