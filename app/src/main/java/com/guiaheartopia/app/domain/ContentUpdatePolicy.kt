package com.guiaheartopia.app.domain

object ContentUpdatePolicy {
    const val INTERVAL_MS: Long = 12L * 60L * 60L * 1000L

    fun isDue(lastAttemptMillis: Long, nowMillis: Long): Boolean =
        lastAttemptMillis == 0L || nowMillis - lastAttemptMillis >= INTERVAL_MS
}
