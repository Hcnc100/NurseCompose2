package com.nullpointer.nourseCompose.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class AppThemeTest {
    @Test
    fun `controls use the same corner radius and dialogs a larger radius`() {
        val size = Size(100f, 100f)
        val density = Density(1f)
        listOf(AppShapes.extraSmall, AppShapes.small, AppShapes.medium, AppShapes.large).forEach {
            assertEquals(16f, it.topStart.toPx(size, density), 0f)
        }
        assertEquals(24f, AppShapes.extraLarge.topStart.toPx(size, density), 0f)
    }

    @Test
    fun `light theme text pairs meet AA contrast`() = assertTextContrast(AppLightColorScheme)

    @Test
    fun `dark theme text pairs meet AA contrast`() = assertTextContrast(AppDarkColorScheme)

    @Test
    fun `success status is readable on light and dark cards`() {
        assertContrast("light success", SuccessLight, AppLightColorScheme.surfaceContainer)
        assertContrast("dark success", SuccessDark, AppDarkColorScheme.surfaceContainer)
    }

    private fun assertTextContrast(scheme: ColorScheme) {
        val pairs = listOf(
            Triple("primary", scheme.onPrimary, scheme.primary),
            Triple("primary container", scheme.onPrimaryContainer, scheme.primaryContainer),
            Triple("secondary", scheme.onSecondary, scheme.secondary),
            Triple("secondary container", scheme.onSecondaryContainer, scheme.secondaryContainer),
            Triple("tertiary", scheme.onTertiary, scheme.tertiary),
            Triple("tertiary container", scheme.onTertiaryContainer, scheme.tertiaryContainer),
            Triple("error", scheme.onError, scheme.error),
            Triple("error container", scheme.onErrorContainer, scheme.errorContainer),
            Triple("body", scheme.onBackground, scheme.background),
            Triple("surface", scheme.onSurface, scheme.surface),
            Triple("card", scheme.onSurface, scheme.surfaceContainer),
            Triple("navigation inactive", scheme.onSurfaceVariant, scheme.surfaceContainer),
            Triple("supporting", scheme.onSurfaceVariant, scheme.surfaceVariant),
            Triple("inverse", scheme.inverseOnSurface, scheme.inverseSurface),
            Triple("text action", scheme.primary, scheme.surface),
            Triple("card action", scheme.primary, scheme.surfaceContainer),
        )
        pairs.forEach { (name, foreground, background) -> assertContrast(name, foreground, background) }
    }

    private fun assertContrast(name: String, foreground: Color, background: Color) {
        val a = luminance(foreground)
        val b = luminance(background)
        val contrast = (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
        assertTrue("$name contrast $contrast must be at least 4.5:1", contrast >= 4.5)
    }

    private fun luminance(color: Color): Double {
        fun linear(channel: Float): Double = channel.toDouble().let {
            if (it <= 0.04045) it / 12.92 else ((it + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }
}
