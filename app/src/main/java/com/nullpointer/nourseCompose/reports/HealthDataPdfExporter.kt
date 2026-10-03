package com.nullpointer.nourseCompose.reports

import android.content.Context
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import java.io.OutputStream

object HealthDataPdfExporter {
    fun write(context: Context, measures: List<MeasureData>, reminders: List<MedicationReminderEntity>, output: OutputStream) {
        ReportPdfWriter(context).use { report ->
            report.text(context.getString(R.string.pdf_report_title), 24f, true)
            report.text(context.getString(R.string.pdf_report_date, report.date(System.currentTimeMillis())), 10f)
            report.text(context.getString(R.string.pdf_report_app_version, context.getString(R.string.app_name),
                context.packageManager.getPackageInfo(context.packageName, 0).versionName), 10f)
            report.text(context.getString(R.string.pdf_report_disclaimer), 11f)
            report.section(context.getString(R.string.pdf_report_medications))
            MedicationReportExporter.entries(context, report, reminders)
            report.section(context.getString(R.string.pdf_report_measurements))
            if (measures.isEmpty()) report.text(context.getString(R.string.pdf_report_none))
            measures.groupBy { it.type }.forEach { (type, values) ->
                report.section(context.getString(type.titleMeasure), keepWithNext = 300f, role = "H3")
                report.chart(values, if (type == MeasureType.PRESSURE) context.getString(R.string.pdf_chart_pressure_legend)
                    else context.getString(R.string.pdf_chart_unit, type.suffix))
                report.text(context.getString(R.string.pdf_exact_values), 13f, true, role = "H4")
                values.sortedByDescending { it.createAt }.forEach {
                    report.text("${report.date(it.createAt)}  ·  ${it.formattedValue(report.locale)}")
                }
            }
            report.write(output)
        }
    }
}
