package com.nullpointer.nourseCompose.ui.screens.home.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class SelectedState(
    initialValue: Int = 0,
) {
    var currentValueSelected by mutableStateOf(initialValue)
        private set


    fun changeNumberSelected(number: Int) {
        check(number >= 0) {
            "verify the value"
        }

        currentValueSelected = number
    }

    fun clearNumberSelected() {
        currentValueSelected = 0
    }


}

@Composable
fun rememberSelectedState(initialValue: Int = 0): SelectedState =
    remember {
        SelectedState(initialValue)
    }