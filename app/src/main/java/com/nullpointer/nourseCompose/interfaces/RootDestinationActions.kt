package com.nullpointer.nourseCompose.interfaces

import com.nullpointer.nourseCompose.ui.screens.destinations.DirectionDestination

fun interface RootDestinationActions {
    fun navigateTo(destination: DirectionDestination)
}