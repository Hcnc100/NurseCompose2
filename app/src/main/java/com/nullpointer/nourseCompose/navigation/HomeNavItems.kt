package com.nullpointer.nourseCompose.navigation

import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.screens.destinations.DirectionDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.GlucoseScreenDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.OxygenScreenDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.PressureScreenDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.TemperatureScreenDestination

enum class HomeNavItems(
    val title: String,
    val icon: Int,
    val destination: DirectionDestination
) {

    TEMPERATURE(
        title = "Temperature",
        icon = R.drawable.baseline_barcode_reader_24,
        destination = TemperatureScreenDestination
    ),
    OXYGEN(
        title = "Oxygen",
        icon = R.drawable.baseline_bloodtype_24,
        destination = OxygenScreenDestination
    ),
    PRESSURE(
        title = "Pressure",
        icon = R.drawable.outline_boy_24,
        destination = PressureScreenDestination
    ),
    GlUCOSE(
        title = "Glucose",
        destination = GlucoseScreenDestination,
        icon = R.drawable.baseline_flare_24
    )

}