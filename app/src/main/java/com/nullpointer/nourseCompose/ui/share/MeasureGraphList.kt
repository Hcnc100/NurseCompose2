package com.nullpointer.nourseCompose.ui.share

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nullpointer.nourseCompose.models.data.MeasureData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeasureGraphList(
    modifier: Modifier = Modifier,
    measureList: List<MeasureData>?,
    lazyListState: LazyListState,
) {

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            measureList == null -> CircularProgressIndicator()
            measureList.isEmpty() -> Text(text = "Without Data")
            else -> LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                stickyHeader {
                    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                        Card(
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            MeasureGraph(
                                measureList = measureList.slice(0..10),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }

                items(
                    measureList,
                    key = { it.id },
                    contentType = { it.type },
                ) {
                    MeasureItem(
                        measureData = it,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }


}