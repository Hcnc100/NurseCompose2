package com.nullpointer.nourseCompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Material3DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = PrimaryVariant,
    background = androidx.compose.ui.graphics.Color(0xFF15131A),
    surface = androidx.compose.ui.graphics.Color(0xFF24212A),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF35313D),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFFF1EDF4)
)

private val Material3LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = PrimaryVariant,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    androidx.compose.material3.MaterialTheme(
        colorScheme = if (darkTheme) Material3DarkColorScheme else Material3LightColorScheme,
        typography = Typography,
        content = content
    )
}
