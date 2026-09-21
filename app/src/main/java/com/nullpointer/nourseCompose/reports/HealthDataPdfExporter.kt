package com.nullpointer.nourseCompose.reports

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import java.io.OutputStream
import java.text.DateFormat
import java.util.Date

object HealthDataPdfExporter {
    fun write(context: Context, measures: List<MeasureData>, reminders: List<MedicationReminderEntity>, output: OutputStream) {
        val document = PdfDocument(); var pageNo = 0; var page: PdfDocument.Page? = null; var y = 0f
        val title = Paint(1).apply { typeface = Typeface.DEFAULT_BOLD; textSize = 24f; color = 0xff29232f.toInt() }
        val heading = Paint(1).apply { typeface = Typeface.DEFAULT_BOLD; textSize = 15f; color = 0xff29232f.toInt() }
        val body = Paint(1).apply { textSize = 10f; color = 0xff3f3945.toInt() }
        val muted = Paint(1).apply { textSize = 9f; color = 0xff746b78.toInt() }
        fun newPage() { page?.let { document.finishPage(it) }; page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, ++pageNo).create()); y = 42f }
        fun text(value: String, paint: Paint = body, gap: Float = 16f) { if (y > 790f) newPage(); page!!.canvas.drawText(value.take(98), 42f, y, paint); y += gap }
        fun section(label: String, color: Int) { if (y > 750f) newPage(); val bar = Paint(1).apply { this.color = color }; page!!.canvas.drawRoundRect(RectF(42f, y - 17f, 553f, y + 12f), 6f, 6f, bar); val white = Paint(heading).apply { this.color = android.graphics.Color.WHITE; textSize = 13f }; page!!.canvas.drawText(label, 54f, y + 2f, white); y += 35f }
        newPage(); text(context.getString(R.string.pdf_report_title), title, 30f); text(context.getString(R.string.pdf_report_date, DateFormat.getDateTimeInstance().format(Date())), muted); text(context.getString(R.string.pdf_report_app_version, context.getString(R.string.app_name), context.packageManager.getPackageInfo(context.packageName, 0).versionName), muted, 22f); text(context.getString(R.string.pdf_report_disclaimer), muted, 28f)
        section(context.getString(R.string.pdf_report_medications), 0xffb34766.toInt())
        reminders.forEach { reminder -> text(reminder.name, heading, 15f); text(context.getString(R.string.pdf_report_medication_details, reminder.dosage ?: context.getString(R.string.pdf_no_dosage), reminder.intervalHours, DateFormat.getDateTimeInstance().format(Date(reminder.startAt))), body, 14f); reminder.photoUri?.let { uri -> runCatching { context.contentResolver.openInputStream(Uri.parse(uri))?.use(BitmapFactory::decodeStream) }.getOrNull()?.let { bitmap -> if (y > 680f) newPage(); page!!.canvas.drawBitmap(bitmap, null, RectF(42f, y, 134f, y + 92f), body); y += 104f } }; y += 5f }
        section(context.getString(R.string.pdf_report_measurements), 0xffff6680.toInt())
        measures.groupBy { it.type }.forEach { (type, values) -> section(context.getString(type.titleMeasure), type.color1); values.sortedByDescending { it.createAt }.forEach { measure -> text("${measure.showValue}  •  ${DateFormat.getDateTimeInstance().format(Date(measure.createAt))}", body, 14f) }; y += 8f }
        page?.let { document.finishPage(it) }; document.writeTo(output); document.close()
    }
}
