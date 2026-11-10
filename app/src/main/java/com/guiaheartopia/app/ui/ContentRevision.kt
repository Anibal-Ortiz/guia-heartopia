package com.guiaheartopia.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guiaheartopia.app.data.ContentSignals

@Composable
fun contentRevision(): Int {
    val tick by ContentSignals.flow().collectAsStateWithLifecycle()
    return tick
}
