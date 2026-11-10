package com.guiaheartopia.app.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.guiaheartopia.app.domain.ContentSyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ContentSignals {
    private val ticks = MutableStateFlow(0)
    private var seenVersion = -1

    fun flow(): StateFlow<Int> = ticks

    fun note(context: Context) {
        val version = AndroidContentStore.installedVersion(context)
        if (version != seenVersion) {
            seenVersion = version
            ticks.value = ticks.value + 1
        }
    }
}

object ContentUpdateScheduler {
    private const val WORK = "content-update"
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun enqueueIfDue(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        val last = AndroidContentStore.lastAttemptMillis(context)
        if (last != 0L && nowMillis - last < DAY_MS) return
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
        if (last != 0L && now - last < 24L * 60L * 60L * 1000L) return Result.success()
        val report = AndroidContentStore.syncNow(applicationContext)
        ContentCheckStore.record(applicationContext, report, now)
        if (report.result == ContentSyncResult.APPLIED) {
            ContentSignals.note(applicationContext)
        }
        return Result.success()
    }
}
