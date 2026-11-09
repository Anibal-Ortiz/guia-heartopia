package com.guiaheartopia.app.ui

import com.guiaheartopia.app.ui.components.NavIcon
import com.guiaheartopia.app.ui.components.NavItem

object AppNavigation {
    const val HOME_ROUTE = "home"

    val BottomNavTabs: List<NavItem> = listOf(
        NavItem(HOME_ROUTE, "Inicio", NavIcon.HOME),
        NavItem("guide", "Guía", NavIcon.GUIDE),
        NavItem("calendar", "Calendario", NavIcon.CALENDAR),
        NavItem("hobbies", "Aficiones", NavIcon.HOBBIES),
    )
}
