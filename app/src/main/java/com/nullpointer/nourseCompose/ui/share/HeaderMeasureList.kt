package com.nullpointer.nourseCompose.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.nullpointer.nourseCompose.models.data.MeasureData
import kotlinx.coroutines.flow.debounce

@Composable
fun HeaderMeasureList(
    graphHeader: @Composable () -> Unit,
    measureList: LazyPagingItems<MeasureData>,
) {

    var countItems by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(key1 = measureList) {
        snapshotFlow { measureList.itemCount }
            .debounce(300)
            .collect {
                countItems = it
            }
    }

    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {

        Card(
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier) {
                graphHeader()
                Text(
                    text = "Number of measures loaded $countItems",
                    Modifier.padding(10.dp),
                    style = TextStyle(
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}