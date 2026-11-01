package com.portproof.core.testing

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onRoot
import java.io.File

/**
 * Writes a PNG of the current content to `<module>/build/screenshots/<name>.png`.
 * Requires `@GraphicsMode(GraphicsMode.Mode.NATIVE)` on the Robolectric test. These renders are for
 * design review (see docs/screenshots); they are not pixel-comparison assertions.
 */
fun ComposeTestRule.saveScreenshot(name: String) {
    waitForIdle()
    val bitmap = onRoot().captureToImage().asAndroidBitmap()
    val dir = File(System.getProperty("user.dir"), "build/screenshots").apply { mkdirs() }
    File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
}
