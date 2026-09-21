package com.nullpointer.nourseCompose.reports

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.content.Context
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import java.io.OutputStream
import java.text.DateFormat
import java.util.Date

/** Exports user-entered reminder data only; it is not a medical report. */
object MedicationReportExporter {
    fun write(context: Context, reminders: List<MedicationReminderEntity>, output: OutputStream) {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 14f }
        var y = 48f
        canvas.drawText(context.getString(R.string.pdf_medication_title), 40f, y, paint); y += 28f
        paint.textSize = 10f
        canvas.drawText(context.getString(R.string.pdf_medication_disclaimer), 40f, y, paint); y += 28f
        reminders.forEach { reminder ->
            if (y > 790f) { document.finishPage(page); document.writeTo(output); return }
            canvas.drawText(context.getString(R.string.pdf_medication_entry, reminder.name, reminder.dosage ?: context.getString(R.string.pdf_no_dosage), reminder.intervalHours), 40f, y, paint); y += 15f
            canvas.drawText(context.getString(R.string.pdf_start_date, DateFormat.getDateTimeInstance().format(Date(reminder.startAt))), 52f, y, paint); y += 18f
        }
        document.finishPage(page)
        document.writeTo(output)
        document.close()
    }
}
