package com.guiaheartopia.app.domain

data class ReminderSyncPlan(
    val keep: Set<String>,
    val enqueue: List<PlannedNotice>,
    val cancel: Set<String>,
)

/**
 * Reprogramar conserva los avisos que siguen valiendo y no vuelve a encolar
 * uno que ya se mostró. Solo cancela los que salieron del plan.
 */
object ReminderSync {
    fun plan(
        previousEnqueued: Set<String>,
        alreadyPosted: Set<String>,
        notices: List<PlannedNotice>,
    ): ReminderSyncPlan {
        val unique = notices.distinctBy { it.id }
        val wanted = unique.map { it.id }.toSet()
        val cancel = (previousEnqueued - wanted) - alreadyPosted
        val keep = previousEnqueued intersect wanted
        val enqueue = unique.filter { it.id !in keep && it.id !in alreadyPosted }
        return ReminderSyncPlan(keep = keep, enqueue = enqueue, cancel = cancel)
    }
}
