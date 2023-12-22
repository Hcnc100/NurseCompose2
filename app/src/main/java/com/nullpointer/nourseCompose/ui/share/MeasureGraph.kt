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
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType

/**
 * Composable function to create a graph for a given measure type and list of measure data.
 *
 * @param measureType The type of measure to be graphed.
 * @param measureList The list of measure data to be graphed.
 * @param modifier The modifier to be applied to the graph.
 * @param textColor The color of the text in the graph.
 */
@Composable
fun MeasureGraph(
    measureType: MeasureType,
    measureList: List<MeasureData>,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colors.onBackground
) {

    // Remember the measure data for the graph
    val measureDataGraph = remember(measureList) {
        LineData().apply {
            measureListToLineDataSet(
                measureType = measureType,
                textColor = textColor,
                measureColor = measureType.color1,
                measureSequence = measureList.asSequence().map { it.value1 }
            )?.let(::addDataSet)

            measureListToLineDataSet(
                measureType = measureType,
                textColor = textColor,
                measureColor = measureType.color2,
                measureSequence = measureList.asSequence().mapNotNull { it.value2 }
            )?.let(::addDataSet)
        }
    }

    // Create the Android view for the graph
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {

                // Configure the graph
                setBasicConfigGraph(this, textColor)

                // Configure the axis style
                setBasicConfigAxis(axisLeft, textColor)
                setBasicConfigAxis(axisRight, textColor)

                // Configure the measure limits
                addMeasureLimits(this, measureType)

                // Add the initial data
                data = measureDataGraph
            }
        },
        update = {
            it.data = measureDataGraph
            it.notifyDataSetChanged()
            it.invalidate()
        }

    )
}

/**
 * Function to convert a sequence of measure data to a line data set.
 *
 * @param textColor The color of the text in the data set.
 * @param measureColor The color of the measure in the data set.
 * @param measureType The type of measure in the data set.
 * @param measureSequence The sequence of measure data.
 * @return The line data set.
 */
private fun measureListToLineDataSet(
    textColor: Color,
    measureColor: Int,
    measureType: MeasureType,
    measureSequence: Sequence<Float>,
): LineDataSet? {
    val measureEntryData = measureSequence.mapIndexed { index, measureData ->
        Entry(index.toFloat(), measureData)
    }.toList()

    if (measureEntryData.isEmpty()) return null

    return LineDataSet(
        measureEntryData,
        measureType.name
    ).apply {
        valueTextSize = 9f
        valueTypeface = Typeface.DEFAULT_BOLD
        color = measureColor
        setCircleColor(measureColor)
        valueTextColor = textColor.toArgb()
    }

}

/**
 * Function to add measure limits to a line chart.
 *
 * @param lineChart The line chart to add the measure limits to.
 * @param measureType The type of measure for the limits.
 */
private fun addMeasureLimits(
    lineChart: LineChart,
    measureType: MeasureType
) = with(measureType) {
    listOf(
        minValue1 to color1,
        maxValue1 to color1,
        minValue2 to color2,
        maxValue2 to color2
    ).forEach { (value, color) ->
        value?.let {
            addAxisYLimit(
                limitValue = it,
                color = color,
                lineChart = lineChart
            )
        }
    }
}

/**
 * Function to add a limit line to a line chart.
 *
 * @param color The color of the limit line.
 * @param limitValue The value of the limit line.
 * @param lineChart The line chart to add the limit line to.
 */
private fun addAxisYLimit(
    color: Int,
    limitValue: Float,
    lineChart: LineChart
) = with(lineChart) {
    val limitLine = LimitLine(limitValue).apply {
        lineWidth = 2f
        lineColor = color
        enableDashedLine(10f, 10f, 0f)
    }
    axisLeft.addLimitLine(limitLine)
}

/**
 * Function to set the basic configuration for a line chart.
 *
 * @param lineChart The line chart to configure.
 * @param textColor The color of the text in the line chart.
 */
private fun setBasicConfigGraph(lineChart: LineChart, textColor: Color) = with(lineChart) {
    // Remove descriptions and legends
    description = Description().apply {
        text = ""
    }
    legend.isEnabled = false
    xAxis.setDrawLabels(false)

    // Configure grids and borders
    setDrawGridBackground(false)
    setDrawBorders(true)
    setBorderColor(textColor.toArgb())

    // Other configurations
    setTouchEnabled(false)

}

/**
 * Function to set the basic configuration for an axis.
 *
 * @param axisBase The axis to configure.
 * @param textColor The color of the text in the axis.
 */
private fun setBasicConfigAxis(axisBase: AxisBase, textColor: Color) = with(axisBase) {
    xOffset = 15f
    setTextColor(textColor.toArgb())
}
