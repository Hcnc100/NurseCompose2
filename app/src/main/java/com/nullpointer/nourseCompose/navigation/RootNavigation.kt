package com.nullpointer.nourseCompose.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

/** Root navigator used by screens hosted inside nested navigation graphs. */
val LocalRootNavController = staticCompositionLocalOf<NavHostController> {
    error("The root navigation controller was not provided")
}
