package com.guiaheartopia.app.ui

data class TabNavigationPolicy(
    val popUpToRoute: String,
    val inclusive: Boolean,
    val saveState: Boolean,
    val restoreState: Boolean,
)

object NavigationPolicy {
    private const val ROOT = AppNavigation.HOME_ROUTE

    fun onBottomTabSelected(route: String): TabNavigationPolicy = when (route) {
        ROOT -> TabNavigationPolicy(
            popUpToRoute = ROOT,
            inclusive = true,
            saveState = false,
            restoreState = false,
        )
        else -> TabNavigationPolicy(
            popUpToRoute = ROOT,
            inclusive = false,
            saveState = true,
            restoreState = true,
        )
    }
}
