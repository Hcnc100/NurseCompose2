package com.nullpointer.nourseCompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Complete schemes prevent components from inheriting unrelated purple defaults.
internal val AppLightColorScheme = lightColorScheme(
    primary = Color(0xFFAD294D), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E1), onPrimaryContainer = Color(0xFF3B0715),
    inversePrimary = Color(0xFFFFB1C2),
    secondary = Color(0xFF78545E), onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3DAE0), onSecondaryContainer = Color(0xFF2D1820),
    tertiary = Color(0xFF85533D), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCC), onTertiaryContainer = Color(0xFF321307),
    background = Color(0xFFFFF8F9), onBackground = Color(0xFF241A1D),
    surface = Color(0xFFFFF8F9), onSurface = Color(0xFF241A1D),
    surfaceVariant = Color(0xFFF0E0E4), onSurfaceVariant = Color(0xFF554349),
    surfaceTint = Color(0xFFAD294D),
    inverseSurface = Color(0xFF392E32), inverseOnSurface = Color(0xFFFFEDF1),
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF857278), outlineVariant = Color(0xFFD8C2C8), scrim = Color.Black,
    surfaceBright = Color(0xFFFFF8F9), surfaceDim = Color(0xFFE8D6DC),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFFFF0F3),
    surfaceContainer = Color(0xFFFCE9EE), surfaceContainerHigh = Color(0xFFF6E3E8),
    surfaceContainerHighest = Color(0xFFF0DDE2),
)

internal val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB1C2), onPrimary = Color(0xFF650F2B),
    primaryContainer = Color(0xFF87213C), onPrimaryContainer = Color(0xFFFFD9E1),
    inversePrimary = Color(0xFFAD294D),
    secondary = Color(0xFFE0BDC6), onSecondary = Color(0xFF432931),
    secondaryContainer = Color(0xFF5D3E47), onSecondaryContainer = Color(0xFFF3DAE0),
    tertiary = Color(0xFFF3B79D), onTertiary = Color(0xFF4E2817),
    tertiaryContainer = Color(0xFF693D29), onTertiaryContainer = Color(0xFFFFDBCC),
    background = Color(0xFF181114), onBackground = Color(0xFFF0DFE4),
    surface = Color(0xFF181114), onSurface = Color(0xFFF0DFE4),
    surfaceVariant = Color(0xFF554349), onSurfaceVariant = Color(0xFFD8C2C8),
    surfaceTint = Color(0xFFFFB1C2),
    inverseSurface = Color(0xFFF0DFE4), inverseOnSurface = Color(0xFF392E32),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFFA08B92), outlineVariant = Color(0xFF554349), scrim = Color.Black,
    surfaceBright = Color(0xFF403136), surfaceDim = Color(0xFF181114),
    surfaceContainerLowest = Color(0xFF120C0F), surfaceContainerLow = Color(0xFF21191D),
    surfaceContainer = Color(0xFF281F23), surfaceContainerHigh = Color(0xFF33292D),
    surfaceContainerHighest = Color(0xFF3F3438),
)

internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    androidx.compose.material3.MaterialTheme(
        colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
