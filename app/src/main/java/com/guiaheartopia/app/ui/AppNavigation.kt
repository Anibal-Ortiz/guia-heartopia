package com.guiaheartopia.app.ui

import com.guiaheartopia.app.ui.components.NavIcon
import com.guiaheartopia.app.ui.components.NavItem

object AppNavigation {
    /** Pestañas inferiores visibles (sin rutina ni píxel). */
    val BottomNavTabs: List<NavItem> = listOf(
        NavItem("home", "Inicio", NavIcon.HOME),
        NavItem("guide", "Guía", NavIcon.GUIDE),
        NavItem("hobbies", "Aficiones", NavIcon.HOBBIES),
    )
}
