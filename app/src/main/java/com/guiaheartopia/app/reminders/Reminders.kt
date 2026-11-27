package com.guiaheartopia.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.CreatureWindowReminders
import com.guiaheartopia.app.domain.CropReminders
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.EventosDocument
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderPlan
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val PREFS = "guia-avisos"
private const val ENQUEUED = "enqueued"
private const val POSTED = "posted"
private const val PERIODIC_WORK = "recordatorios-periodicos"
/** Ventana en la que un aviso perdido todavía se entrega al sincronizar. */
private const val LATE_GRACE_MINUTES = 30L

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
        // Re-arma el plan aunque la app no se abra: si el sistema pierde las alarmas
        // (Doze, reinicio, limpiador de batería), la próxima ejecución las recupera.
        ensurePeriodicSync(app)
        val store = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = store.getStringSet(ENQUEUED, emptySet()).orEmpty()
        val posted = store.getStringSet(POSTED, emptySet()).orEmpty()
        // Misma resolución que la UI y SkyCatalog: si el id no es válido, todos
        // caen en la misma zona y el plan no se desalinea del pronóstico mostrado.
        val zone = DexSchedule.zoneOf(profile.zoneId)
        val now = ZonedDateTime.now(zone)
        // Generar desde un poco antes permite entregar avisos que no sonaron.
        // Los generadores descartan todo lo anterior a su `now`, así que sin esta
        // ventana `expired` siempre estaba vacío y el aviso se perdía para siempre.
        val from = now.minusMinutes(LATE_GRACE_MINUTES)
        val prefs = profile.reminders
        val scheduled = GuideRepository(app).snapshots.first()
        val personal = CropReminders.notices(scheduled.activeCrops, zone, from) +
            CreatureWindowReminders.notices(scheduled.creatureWindowAlarms, zone, from)
        val notices = if (!prefs.anyAlertOn()) {
            personal
        } else {
            val weather = if (prefs.weatherEnabled) {
                SkyCatalog.load(app, profile.zoneId).let { sky ->
                    ReminderPlan.weatherNotices(
                        (0..7).mapNotNull { sky.on(from.toLocalDate().plusDays(it.toLong())) },
                        zone,
                        from,
                    )
                }
            } else {
                emptyList()
            }
            val raw = AndroidContentLibrary(app).effectiveBytes("calendar/eventos.json")?.decodeToString()
            val events = ReminderPlan.eventNotices(parseEventsCached(raw), prefs, zone, from)
            weather + events + personal
        }
        val future = notices.filter { it.fireAt.isAfter(now) }
        val expired = notices.filter { !it.fireAt.isAfter(now) }
        val stillPosted = posted.toMutableSet()
        expired.forEach { notice ->
            // Un aviso tardío que no está en POSTED nunca se entregó (Doze,
            // reinicio o alarma inexacta). Marcarlo como publicado sin mostrarlo
            // es lo que hacía que los avisos de clima desaparecieran sin sonar.
            if (notice.id !in posted) {
                runCatching {
                    ReminderNotifications.show(app, notice.title, notice.body, notice.id)
                }
            }
            stillPosted += notice.id
        }

        // Reprogramar SIEMPRE todas las alarmas futuras. AlarmManager.setExact con el
        // mismo requestCode sobrescribe la alarma previa, así que es idempotente.
        // Esto recupera alarmas perdidas tras force-stop/reinicio: el set ENQUEUED
        // persiste pero el AlarmManager se vacía, y `keep` solía impedir re-agendarlas.
        val wantedFutureIds = future.map { it.id }.toSet()
        val toCancel = (previous - wantedFutureIds) - stillPosted
        toCancel.forEach { ReminderAlarms.cancel(app, it) }
        future.forEach { ReminderAlarms.schedule(app, it) }

        store.edit()
            .putStringSet(ENQUEUED, wantedFutureIds)
            .putStringSet(POSTED, stillPosted.intersect(wantedFutureIds + expired.map { it.id }.toSet()))
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

    private fun ensurePeriodicSync(app: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderPeriodicSyncWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(app).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
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

/** Vuelve a armar los avisos y entrega los que se quedaron sin sonar. */
class ReminderPeriodicSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
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
