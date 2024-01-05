package com.nullpointer.nourseCompose.navigation

import androidx.annotation.StringRes
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.screens.destinations.DirectionDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.GlucoseScreenDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.OxygenScreenDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.PressureScreenDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.TemperatureScreenDestination

enum class HomeNavItems(
    @StringRes
    val title: Int,
    val icon: Int,
    val destination: DirectionDestination
) {

    TEMPERATURE(
        title = R.string.title_temperature,
        icon = R.drawable.baseline_barcode_reader_24,
        destination = TemperatureScreenDestination
    ),
    OXYGEN(
        title = R.string.title_oxygen,
        icon = R.drawable.baseline_bloodtype_24,
        destination = OxygenScreenDestination
    ),
    PRESSURE(
        title = R.string.title_pressure,
        icon = R.drawable.outline_boy_24,
        destination = PressureScreenDestination
    ),
    GLUCOSE(
        title = R.string.title_glucose,
        destination = GlucoseScreenDestination,
        icon = R.drawable.baseline_flare_24
    )

}