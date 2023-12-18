package com.nullpointer.nourseCompose.ui.share

import android.graphics.Color
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.nullpointer.nourseCompose.models.data.MeasureData

@Composable
fun MeasureGraph(
    measureList: List<MeasureData>,
    modifier: Modifier = Modifier,
) {

    val measureType = measureList.first().type

    val measureEntry = remember(measureList) {
        measureList.reversed().mapIndexed { index, measureData ->
            Entry(index.toFloat(), measureData.value)
        }
    }

    val measureDataSet = remember(measureEntry) {
        LineDataSet(
            measureEntry,
            measureType.name
        ).apply {
            valueTextSize = 9f
            valueTypeface = Typeface.DEFAULT_BOLD
        }
    }

    val measureDataGraph = remember(measureDataSet) {
        LineData(measureDataSet)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                data = measureDataGraph
                description = Description().apply {
                    text = ""
                }


                setDrawGridBackground(false)
                setDrawBorders(true)

                xAxis.setDrawLabels(false)
                legend.isEnabled = false

                // Set the border color to black
                setBorderColor(Color.BLACK)

                axisLeft.xOffset = 15f
                axisRight.xOffset = 15f

                setTouchEnabled(false)

                measureType.maxValue?.let {
                    val limitLine = LimitLine(it).apply {
                        lineWidth = 2f
                        lineColor = Color.RED
                        enableDashedLine(10f, 10f, 0f)
                    }
                    axisLeft.addLimitLine(limitLine)
                }

                measureType.minValue?.let {
                    val limitLine = LimitLine(it).apply {
                        lineWidth = 2f
                        lineColor = Color.BLUE
                        enableDashedLine(10f, 10f, 0f)
                    }
                    axisLeft.addLimitLine(limitLine)
                }


            }
        },
        update = {
            it.data = measureDataGraph
            it.notifyDataSetChanged()
            it.invalidate()
        }

    )
}