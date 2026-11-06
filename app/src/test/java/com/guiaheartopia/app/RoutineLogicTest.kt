package com.guiaheartopia.app

import com.guiaheartopia.app.domain.RoutineLogic
import com.guiaheartopia.app.domain.RoutineState
import com.guiaheartopia.app.domain.RoutineTask
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineLogicTest {
    private val tasks = listOf(RoutineTask("a", "Una"), RoutineTask("b", "Dos"))
    private val day = LocalDate.of(2026, 11, 6)

    @Test
    fun emptyListHasNoProgress() {
        assertEquals(0f, RoutineLogic.fraction(RoutineState()))
    }

    @Test
    fun partialProgressUsesOnlyExistingTasks() {
        val state = RoutineState(epochDay = day.toEpochDay(), tasks = tasks, completedIds = listOf("a"), initialized = true)
        assertEquals(0.5f, RoutineLogic.fraction(state))
        assertEquals(1, RoutineLogic.completedCount(state))
    }

    @Test
    fun sameLocalDateKeepsChecks() {
        val state = RoutineState(epochDay = day.toEpochDay(), tasks = tasks, completedIds = listOf("b"), initialized = true)
        val next = RoutineLogic.prepare(state, day, tasks)
        assertEquals(listOf("b"), next.completedIds)
    }

    @Test
    fun nextLocalDateClearsChecksAndKeepsTasks() {
        val state = RoutineState(epochDay = day.toEpochDay(), tasks = tasks, completedIds = listOf("a", "b"), initialized = true)
        val next = RoutineLogic.prepare(state, day.plusDays(1), tasks)
        assertTrue(next.completedIds.isEmpty())
        assertEquals(tasks, next.tasks)
        assertEquals(day.plusDays(1).toEpochDay(), next.epochDay)
    }

    @Test
    fun restoreDayClearsChecksWithoutRemovingTasks() {
        val state = RoutineState(epochDay = day.toEpochDay(), tasks = tasks, completedIds = listOf("a"), initialized = true)
        val restored = RoutineLogic.restoreDay(state)
        assertTrue(restored.completedIds.isEmpty())
        assertEquals(tasks, restored.tasks)
        assertEquals(state.epochDay, restored.epochDay)
    }

    @Test
    fun firstPrepareCopiesSuggestions() {
        val next = RoutineLogic.prepare(RoutineState(), day, tasks)
        assertEquals(tasks, next.tasks)
        assertTrue(next.initialized)
        assertTrue(next.completedIds.isEmpty())
    }

    @Test
    fun toggleAddsAndRemoves() {
        val start = RoutineState(epochDay = day.toEpochDay(), tasks = tasks, initialized = true)
        val on = RoutineLogic.toggle(start, "a")
        val off = RoutineLogic.toggle(on, "a")
        assertEquals(listOf("a"), on.completedIds)
        assertTrue(off.completedIds.isEmpty())
    }
}
