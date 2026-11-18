package com.guiaheartopia.app.domain

object ContentUpdatePolicy {
    const val INTERVAL_MS: Long = 12L * 60L * 60L * 1000L

    fun isDue(lastAttemptMillis: Long, nowMillis: Long): Boolean {
        if (lastAttemptMillis == 0L) return true
        // Reloj hacia atrás: se considera vencido para no bloquear la sincronización.
        if (nowMillis < lastAttemptMillis) return true
        return nowMillis - lastAttemptMillis >= INTERVAL_MS
    }
}
