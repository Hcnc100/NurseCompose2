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
    // #FF6680 needs a dark foreground to meet the 4.5:1 contrast target for normal text.
    onPrimary = androidx.compose.ui.graphics.Color(0xFF3B0715),
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFFF1EDF4)
)

private val Material3LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = PrimaryVariant,
    // Keep foregrounds consistent with the dark scheme and avoid white on the pink primary.
    onPrimary = androidx.compose.ui.graphics.Color(0xFF3B0715),
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
