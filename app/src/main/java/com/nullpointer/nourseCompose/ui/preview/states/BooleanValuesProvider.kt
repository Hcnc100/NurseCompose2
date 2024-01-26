package com.nullpointer.nourseCompose.ui.preview.states

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class BooleanValuesProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(true, false)
}
