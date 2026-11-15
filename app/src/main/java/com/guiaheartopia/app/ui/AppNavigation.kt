package com.guiaheartopia.app.ui

import com.guiaheartopia.app.ui.components.NavIcon
import com.guiaheartopia.app.ui.components.NavItem

object AppNavigation {
    const val HOME_ROUTE = "home"

    val BottomNavTabs: List<NavItem> = listOf(
        NavItem(HOME_ROUTE, "Inicio", NavIcon.HOME),
        NavItem("aficiones", "Aficiones", NavIcon.HOBBIES),
        NavItem("codigos", "Códigos", NavIcon.CODES),
        NavItem("calendar", "Calendario", NavIcon.CALENDAR),
    )

    fun tabForRoute(route: String?): String? = when {
        route == null || route == "settings" || route.startsWith("settings/") ||
            route == "map" || route == "photo-credits" -> null
        route == HOME_ROUTE || route.startsWith("home") -> HOME_ROUTE
        route == "codigos" || route.startsWith("codigos/") -> "codigos"
        route == "calendar" || route.startsWith("calendar") || route == "weather" -> "calendar"
        route == "aficiones" || route.startsWith("guide") || route.startsWith("dex/") ||
            route.startsWith("animal/") || route.startsWith("huerto") ||
            route.startsWith("cocina") || route.startsWith("hobbies") ||
            route.startsWith("niveles") -> "aficiones"
        else -> null
    }
}
