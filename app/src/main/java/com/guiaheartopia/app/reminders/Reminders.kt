package com.guiaheartopia.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.CreatureWindowReminders
import com.guiaheartopia.app.domain.CropReminders
import com.guiaheartopia.app.domain.EventosDocument
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.ReminderSync
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val PREFS = "guia-avisos"
private const val ENQUEUED = "enqueued"
private const val POSTED = "posted"

object ReminderScheduler {
    // Coalescing: gana la sincronización más reciente; las intermedias no se ejecutan.
    private val syncSequence = AtomicLong(0L)
    private val latestSync = AtomicLong(0L)
    private val syncMutex = Mutex()

    // Cache del parseo de eventos: el JSON remoto cambia muy poco y se lee en cada sync.
    @Volatile
    private var cachedEventsRaw: String? = null
    @Volatile
    private var cachedEvents: EventosDocument? = null

    /**
     * Coalesce las ráfagas de llamadas (perfil, contenido, alarma) en una sola
     * sincronización con el profile más reciente. Firma sin cambios para ui/ y data/.
     */
    suspend fun sync(context: Context, profile: PlayerProfile) = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        val token = syncSequence.incrementAndGet()
        latestSync.set(token)
        syncMutex.withLock {
            if (token != latestSync.get()) return@withLock
            performSync(app, profile)
        }
    }

    private suspend fun performSync(app: Context, profile: PlayerProfile) {
        val store = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = store.getStringSet(ENQUEUED, emptySet()).orEmpty()
        val posted = store.getStringSet(POSTED, emptySet()).orEmpty()
        val zone = runCatching { ZoneId.of(profile.zoneId) }.getOrElse { ZoneId.systemDefault() }
        val now = ZonedDateTime.now(zone)
        val prefs = profile.reminders
        val scheduled = GuideRepository(app).snapshots.first()
        val personal = CropReminders.notices(scheduled.activeCrops, zone, now) +
            CreatureWindowReminders.notices(scheduled.creatureWindowAlarms, zone, now)
        val notices = if (!prefs.anyAlertOn()) {
            personal
        } else {
            val weather = if (prefs.weatherEnabled) {
                ReminderPlan.weatherNotices(SkyCatalog.load(app).days.values, zone, now)
            } else {
                emptyList()
            }
            val raw = AndroidContentLibrary(app).effectiveBytes("calendar/eventos.json")?.decodeToString()
            val events = ReminderPlan.eventNotices(parseEventsCached(raw), prefs, zone, now)
            weather + events + personal
        }
        val future = notices.filter { it.fireAt.isAfter(now) }
        val expired = notices.filter { !it.fireAt.isAfter(now) }
        val stillPosted = posted.toMutableSet()
        expired.forEach { stillPosted += it.id }
        val diff = ReminderSync.plan(previous, stillPosted, future)
        diff.cancel.forEach { ReminderAlarms.cancel(app, it) }
        diff.enqueue.forEach { ReminderAlarms.schedule(app, it) }
        val stillWanted = future.map { it.id }.toSet()
        store.edit()
            .putStringSet(ENQUEUED, diff.keep + diff.enqueue.map { it.id })
            .putStringSet(POSTED, stillPosted.intersect(stillWanted + expired.map { it.id }.toSet()))
            .apply()
    }

    private fun parseEventsCached(raw: String?): EventosDocument? {
        if (raw.isNullOrBlank()) return null
        if (raw == cachedEventsRaw) return cachedEvents
        return ReminderPlan.parseEvents(raw).also {
            cachedEventsRaw = raw
            cachedEvents = it
        }
    }

    fun markPosted(context: Context, id: String) {
        if (id.isBlank()) return
        val store = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val posted = store.getStringSet(POSTED, emptySet()).orEmpty() + id
        val enqueued = store.getStringSet(ENQUEUED, emptySet()).orEmpty() - id
        store.edit()
            .putStringSet(POSTED, posted)
            .putStringSet(ENQUEUED, enqueued)
            .apply()
    }
}

class ReminderRescheduleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val profile = GuideRepository(applicationContext).snapshots.first().profile
        ReminderScheduler.sync(applicationContext, profile)
        return Result.success()
    }
}

/**
 * Reprograma los avisos tras un reinicio del dispositivo.
 *
 * Recomendación de manifiesto (archivo fuera de este módulo): el <receiver> debe
 * declarar android:permission="android.permission.RECEIVE_BOOT_COMPLETED" para no
 * aceptar emisiones falsas de otras apps (el permiso uses-permission ya existe).
 * BOOT_COMPLETED es un broadcast protegido, así que también puede ponerse
 * android:exported="false" si se quiere reducir la superficie de exposición.
 */
class BootReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val request = OneTimeWorkRequestBuilder<ReminderRescheduleWorker>().build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            "recordatorios-tras-reinicio",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
