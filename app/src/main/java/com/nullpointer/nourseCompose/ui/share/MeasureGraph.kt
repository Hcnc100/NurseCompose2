package com.nullpointer.nourseCompose.ui.share

import android.graphics.Typeface
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType

@Composable
fun MeasureGraph(
    measureList: List<MeasureData>,
    modifier: Modifier = Modifier,
    measureType: MeasureType,
    textColor: Color = MaterialTheme.colors.onBackground
) {


    val measureEntry1 = remember(measureList) {
        measureList.reversed().mapIndexed { index, measureData ->
            Entry(index.toFloat(), measureData.value1)
        }
    }

    val measureEntry2 = remember(measureList) {
        measureList.mapNotNull { it.value2 }.reversed().mapIndexed { index, measureData ->
            Entry(index.toFloat(), measureData)
        }
    }

    val measureDataSet1 = remember(measureEntry1) {
        LineDataSet(
            measureEntry1,
            measureType.name
        ).apply {
            valueTextSize = 9f
            valueTypeface = Typeface.DEFAULT_BOLD
            color = measureType.color1
            setCircleColor(measureType.color1)
            valueTextColor = textColor.hashCode()
        }
    }

    val measureDataSet2 = remember(measureEntry2) {
        LineDataSet(
            measureEntry2,
            measureType.name
        ).apply {
            valueTextSize = 9f
            valueTypeface = Typeface.DEFAULT_BOLD
            measureType.color2?.let { color2 ->
                color = color2
                setCircleColor(color2)
            }
        }
    }


    val measureDataGraph = remember(measureDataSet1) {
        LineData(measureDataSet1, measureDataSet2)
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
                setBorderColor(textColor.toArgb())

                axisLeft.xOffset = 15f
                axisRight.xOffset = 15f

                axisRight.textColor = textColor.toArgb()
                axisLeft.textColor = textColor.toArgb()

                setTouchEnabled(false)

                measureType.minValue1?.let {
                    val limitLine = LimitLine(it).apply {
                        lineWidth = 2f
                        lineColor = measureType.color1
                        enableDashedLine(10f, 10f, 0f)
                    }
                    axisLeft.addLimitLine(limitLine)
                }


                measureType.maxValue1?.let {
                    val limitLine = LimitLine(it, "").apply {
                        lineWidth = 2f
                        lineColor = measureType.color1
                        enableDashedLine(10f, 10f, 0f)
                    }
                    axisLeft.addLimitLine(limitLine)
                }


                measureType.minValue2?.let {
                    val limitLine = LimitLine(it).apply {
                        lineWidth = 2f
                        lineColor = measureType.color2!!
                        enableDashedLine(10f, 10f, 0f)
                    }
                    axisLeft.addLimitLine(limitLine)
                }


                measureType.maxValue2?.let {
                    val limitLine = LimitLine(it).apply {
                        lineWidth = 2f
                        lineColor = measureType.color2!!
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