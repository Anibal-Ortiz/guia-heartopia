package com.guiaheartopia.app.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.guiaheartopia.app.domain.ContentDownloadProgress
import com.guiaheartopia.app.domain.ContentSyncResult
import com.guiaheartopia.app.domain.checkOk
import com.guiaheartopia.app.domain.ContentUpdatePolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow

object ContentSignals {
    private val ticks = MutableStateFlow(0)
    private val checkOkTicks = MutableStateFlow(0)
    private val failed = MutableStateFlow(false)
    private val downloading = MutableStateFlow(false)
    private val downloads = MutableStateFlow<ContentDownloadProgress?>(null)
    private var seenVersion = -1

    fun flow(): StateFlow<Int> = ticks

    /** Se incrementa cuando la verificación terminó bien (APPLIED / UNCHANGED / OLDER). */
    fun checkOkTicks(): StateFlow<Int> = checkOkTicks

    /** Persiste hasta que una búsqueda termine correctamente. */
    fun failed(): StateFlow<Boolean> = failed

    fun setFailed(value: Boolean) {
        failed.value = value
    }

    fun busy(): StateFlow<Boolean> = downloading

    fun progress(): StateFlow<ContentDownloadProgress?> = downloads

    fun setBusy(value: Boolean) {
        downloading.value = value
        if (!value) downloads.value = null
    }

    fun setProgress(value: ContentDownloadProgress?) {
        downloads.value = value
    }

    /** Refresca la UI al instante. Se llama en cada apertura, no implica que haya cambios. */
    fun note(context: Context) {
        seenVersion = AndroidContentStore.installedVersion(context)
        ticks.value = ticks.value + 1
    }

    /** Enciende el check del badge cuando la búsqueda terminó correctamente. */
    fun noteCheckOk() {
        checkOkTicks.value = checkOkTicks.value + 1
    }
}

object ContentUpdateScheduler {
    private const val WORK = "content-update"

    fun enqueueIfDue(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        val last = AndroidContentStore.lastAttemptMillis(context)
        if (!ContentUpdatePolicy.isDue(last, nowMillis)) return
        val request = OneTimeWorkRequestBuilder<ContentUpdateWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK, ExistingWorkPolicy.KEEP, request)
    }
}

class ContentUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val last = AndroidContentStore.lastAttemptMillis(applicationContext)
        if (!ContentUpdatePolicy.isDue(last, now)) return Result.success()
        val report = try {
            AndroidContentStore.syncNow(applicationContext) { ContentSignals.setProgress(it) }
        } finally {
            ContentSignals.setProgress(null)
        }
        ContentCheckStore.record(applicationContext, report, now)
        if (report.result == ContentSyncResult.APPLIED) {
            ContentSignals.note(applicationContext)
        }
        if (report.result.checkOk) {
            ContentSignals.noteCheckOk()
        }
        ContentSignals.setFailed(!report.result.checkOk)
        if (report.result == ContentSyncResult.APPLIED || report.result == ContentSyncResult.UNCHANGED) {
            val profile = GuideRepository(applicationContext).snapshots.first().profile
            com.guiaheartopia.app.reminders.ReminderScheduler.sync(applicationContext, profile)
        }
        return Result.success()
    }
}
