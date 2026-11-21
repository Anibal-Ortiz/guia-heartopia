package com.guiaheartopia.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.Instant

/**
 * Reloj compartido de la app: una única fuente de verdad que avanza al minuto.
 *
 * Todos los widgets que muestran horas del juego (GameTimeCard, Huerto, guía, inicio…)
 * leen de aquí, de modo que nunca se mezclan `Instant.now()` capturados en momentos distintos.
 * El flujo es global y se comparte entre composables y ViewModels.
 */
object GameClock {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val tick: StateFlow<Instant> = flow {
        while (true) {
            emit(Instant.now())
            // Se alinea al siguiente minuto para no acumular deriva.
            delay(60_000L - (System.currentTimeMillis() % 60_000L))
        }
    }.stateIn(scope, SharingStarted.Eagerly, Instant.now())

    /** Tick cada 5 segundos para contadores de cultivos (más fluido). */
    val tick5s: StateFlow<Instant> = flow {
        while (true) {
            emit(Instant.now())
            delay(5_000L)
        }
    }.stateIn(scope, SharingStarted.Eagerly, Instant.now())

    /** Valor actual sin necesidad de estar en composición (útil en ViewModels). */
    fun now(): Instant = tick.value
}

/** Suscribe la composición al tick compartido (1 min) y devuelve el instante vigente. */
@Composable
fun rememberMinuteTick(): Instant = GameClock.tick.collectAsState().value

/** Suscribe la composición al tick de 5 segundos para contadores fluidos. */
@Composable
fun remember5SecondTick(): Instant = GameClock.tick5s.collectAsState().value
