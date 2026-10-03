package com.nullpointer.nourseCompose.reports

import android.content.Context
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import java.io.OutputStream

/** User-entered data only; not a medical report. */
object MedicationReportExporter {
    internal fun entries(context: Context, report: ReportPdfWriter, reminders: List<MedicationReminderEntity>, headingRole: String = "H3") {
        if (reminders.isEmpty()) report.text(context.getString(R.string.pdf_report_none))
        reminders.forEach { reminder ->
            report.section(reminder.name, role = headingRole)
            val schedule = if (reminder.endAt == reminder.startAt) context.getString(R.string.schedule_single_dose)
                else context.getString(R.string.pdf_repeat_interval, reminder.intervalMinutes)
            report.text(context.getString(R.string.pdf_dose_schedule,
                reminder.dosage?.takeIf { it.isNotBlank() } ?: context.getString(R.string.pdf_no_dosage), schedule))
            report.text(context.getString(R.string.pdf_start_date, report.date(reminder.startAt)))
            reminder.endAt?.takeIf { it != reminder.startAt }?.let {
                report.text(context.getString(R.string.pdf_schedule_end, report.date(it)))
            }
            report.text(context.getString(if (reminder.isActive) R.string.pdf_status_active else R.string.pdf_status_paused))
            reminder.comment?.takeIf { it.isNotBlank() }?.let { report.text(context.getString(R.string.pdf_comment, it)) }
            reminder.photoUri?.let { uri ->
                val bitmap = runCatching {
                    val parsed = android.net.Uri.parse(uri)
                    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(parsed)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
                    val options = android.graphics.BitmapFactory.Options().apply {
                        inSampleSize = (maxOf(bounds.outWidth, bounds.outHeight) / 512).coerceAtLeast(1)
                    }
                    context.contentResolver.openInputStream(parsed)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
                }.getOrNull()
                bitmap?.let { try { report.photo(it, context.getString(R.string.pdf_photo_description, reminder.name)) } finally { it.recycle() } }
            }
        }
    }
    fun write(context: Context, reminders: List<MedicationReminderEntity>, output: OutputStream) {
        ReportPdfWriter(context).use { report ->
            report.text(context.getString(R.string.pdf_medication_title), 24f, true)
            report.text(context.getString(R.string.pdf_report_date, report.date(System.currentTimeMillis())), 10f)
            report.text(context.getString(R.string.pdf_medication_disclaimer), 11f)
            entries(context, report, reminders, headingRole = "H2")
            report.write(output)
        }
    }
}
