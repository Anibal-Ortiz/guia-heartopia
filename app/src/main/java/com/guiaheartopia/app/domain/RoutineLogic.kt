package com.guiaheartopia.app.domain

import java.time.LocalDate

/** Progreso y reinicio de la rutina personal. No representa tareas oficiales del juego. */
object RoutineLogic {
    fun fraction(state: RoutineState): Float {
        if (state.tasks.isEmpty()) return 0f
        val done = state.tasks.count { it.id in state.completedIds }
        return done.toFloat() / state.tasks.size
    }

    fun completedCount(state: RoutineState): Int =
        state.tasks.count { it.id in state.completedIds }

    /**
     * Si cambia la fecha local, desmarca las tareas y conserva la lista.
     * La primera vez copia las sugerencias.
     */
    fun prepare(state: RoutineState, today: LocalDate, suggestions: List<RoutineTask>): RoutineState {
        val day = today.toEpochDay()
        val base = if (!state.initialized) {
            state.copy(initialized = true, tasks = suggestions, completedIds = emptyList(), epochDay = day)
        } else {
            state
        }
        if (base.epochDay == day) {
            val valid = base.completedIds.filter { id -> base.tasks.any { it.id == id } }
            return if (valid.size == base.completedIds.size) base else base.copy(completedIds = valid)
        }
        return base.copy(epochDay = day, completedIds = emptyList())
    }

    fun toggle(state: RoutineState, taskId: String): RoutineState {
        if (state.tasks.none { it.id == taskId }) return state
        val done = state.completedIds.toMutableSet()
        if (!done.add(taskId)) done.remove(taskId)
        return state.copy(completedIds = state.tasks.map { it.id }.filter { it in done })
    }

    /** Vuelve a dejar el día en blanco, sin borrar la lista. */
    fun restoreDay(state: RoutineState): RoutineState = state.copy(completedIds = emptyList())

    fun addTask(state: RoutineState, title: String, id: String): RoutineState {
        val clean = title.trim()
        if (clean.isEmpty()) return state
        return state.copy(tasks = state.tasks + RoutineTask(id, clean), initialized = true)
    }

    fun removeTask(state: RoutineState, taskId: String): RoutineState =
        state.copy(
            tasks = state.tasks.filterNot { it.id == taskId },
            completedIds = state.completedIds.filterNot { it == taskId },
            initialized = true,
        )
}
