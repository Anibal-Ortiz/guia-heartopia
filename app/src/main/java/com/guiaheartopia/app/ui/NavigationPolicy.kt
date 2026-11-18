package com.guiaheartopia.app.ui

import com.guiaheartopia.app.ui.AppNavigation.HOME_ROUTE

data class TabNavigationPolicy(
    val popUpToRoute: String,
    val inclusive: Boolean,
    val saveState: Boolean,
    val restoreState: Boolean,
)

object NavigationPolicy {
    private const val AFICIONES_ROUTE = "aficiones"
    private const val CODES_ROUTE = "codigos"
    private const val CALENDAR_ROUTE = "calendar"

    /**
     * Cada pestaña abre la raíz de su sección descartando el stack anterior: `popUpTo` la
     * raíz destino con `inclusive = true` deja una sola entrada, así que Atrás sale de la
     * app en vez de volver a una ficha interna de otra pestaña.
     *
     * No se usa saveState/restoreState: restaurar una pestaña internal contradictoria con
     * "limpiar pila". El estado de cada pantalla ya vive en el ViewModel/DataStore.
     */
    fun onBottomTabSelected(route: String): TabNavigationPolicy {
        val root = when {
            route == HOME_ROUTE || route.startsWith("$HOME_ROUTE/") -> HOME_ROUTE
            route == AFICIONES_ROUTE || route.startsWith("$AFICIONES_ROUTE/") ||
                route.startsWith("guide") || route.startsWith("dex/") ||
                route.startsWith("animal/") || route.startsWith("huerto") ||
                route.startsWith("cocina") || route.startsWith("hobbies") ||
                route.startsWith("niveles") -> AFICIONES_ROUTE
            route == CODES_ROUTE || route.startsWith("$CODES_ROUTE/") -> CODES_ROUTE
            route == CALENDAR_ROUTE || route.startsWith("$CALENDAR_ROUTE/") ||
                route == "weather" -> CALENDAR_ROUTE
            else -> route
        }
        return TabNavigationPolicy(
            popUpToRoute = root,
            inclusive = true,
            saveState = false,
            restoreState = false,
        )
    }
}
