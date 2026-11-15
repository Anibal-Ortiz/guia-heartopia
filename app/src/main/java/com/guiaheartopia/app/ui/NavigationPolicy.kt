package com.guiaheartopia.app.ui

data class TabNavigationPolicy(
    val popUpToRoute: String,
    val inclusive: Boolean,
    val saveState: Boolean,
    val restoreState: Boolean,
)

object NavigationPolicy {
    /**
     * Cada pestaña abre la raíz de su sección y no recupera una ficha interna.
     * Inicio vuelve a Mi día.
     */
    fun onBottomTabSelected(route: String): TabNavigationPolicy = TabNavigationPolicy(
        popUpToRoute = AppNavigation.HOME_ROUTE,
        inclusive = route == AppNavigation.HOME_ROUTE,
        saveState = false,
        restoreState = false,
    )
}
