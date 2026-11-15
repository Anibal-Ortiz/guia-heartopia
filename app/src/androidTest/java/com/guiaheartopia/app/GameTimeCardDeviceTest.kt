package com.guiaheartopia.app

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.guiaheartopia.app.ui.components.GameTimeCard
import com.guiaheartopia.app.ui.theme.GuiaTheme
import java.io.File
import java.time.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameTimeCardDeviceTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun captureChileNov9GameTimeCard() {
        val instant = Instant.parse("2026-11-09T21:31:00Z")
        rule.setContent {
            GuiaTheme {
                GameTimeCard(
                    zoneId = "America/Santiago",
                    modifier = Modifier.padding(16.dp),
                    instant = instant,
                )
            }
        }
        rule.waitForIdle()
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.filesDir, "gametime-chile-2026-11-09-1831.png")
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        check(file.exists() && file.length() > 5_000) {
            "No se escribió la captura en ${file.absolutePath} (tamaño ${file.length()})"
        }
        val export = File("/sdcard/Download/gametime-chile-2026-11-09-1831.png")
        runCatching {
            if (export.exists() && !export.delete()) return@runCatching
            file.copyTo(export, overwrite = true)
        }
    }
}
