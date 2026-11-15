package com.guiaheartopia.app.ads

object AdMobIds {
    const val TEST_APP = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

    fun isPlaceholder(id: String): Boolean {
        val value = id.trim()
        if (value.isEmpty()) return true
        val lower = value.lowercase()
        return lower.contains("xxxx") ||
            lower.contains("yyyy") ||
            lower.contains("placeholder") ||
            lower.contains("tu_id") ||
            lower.contains("reemplazar") ||
            lower.contains("ejemplo") ||
            lower.startsWith("admob_")
    }
}
