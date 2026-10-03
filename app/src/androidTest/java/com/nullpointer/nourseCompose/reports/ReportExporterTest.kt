package com.nullpointer.nourseCompose.reports

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.LocaleList
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.junit.Test
import java.io.File
import java.util.Locale

class ReportExporterTest {
    @Test fun exportsLocalizedChartsAndAllReminderPages() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val now = 1790942400000L
        for (language in listOf("en", "es")) {
            val configuration = Configuration(base.resources.configuration).apply { setLocales(LocaleList(Locale(language))) }
            val context = base.createConfigurationContext(configuration)
            val directory = File(base.getExternalFilesDir(null), "qa-reports").apply { mkdirs() }
            val reminders = (1..120).map {
                MedicationReminderEntity(name = "QA-$it " + "Nombre largo de medicamento ".repeat(5),
                    dosage = "Dosis de ejemplo ".repeat(6), comment = "Nota de prueba ".repeat(12),
                    startAt = now, endAt = if (it == 1) now else null, intervalHours = 8)
            }
            val medication = File(directory, "medications-$language.pdf")
            medication.outputStream().use { MedicationReportExporter.write(context, reminders, it) }
            val measures = MeasureType.values().flatMap { type ->
                (0..7).map { i -> MeasureData(i, if (type == MeasureType.TEMPERATURE) 36f + i / 10f else 90f + i,
                    if (type == MeasureType.PRESSURE && i != 3) 65f + i else null,
                    now + i.toLong() * i * 3600000L, type) }
            }
            val health = File(directory, "health-$language.pdf")
            health.outputStream().use { HealthDataPdfExporter.write(context, measures, reminders.take(1), it) }
            for (file in listOf(medication, health)) {
                PDDocument.load(file).use { pdf ->
                    assertEquals(language, pdf.documentCatalog.language)
                    assertTrue(pdf.documentCatalog.markInfo.isMarked)
                    assertNotNull(pdf.documentCatalog.structureTreeRoot)
                    assertNotNull(pdf.documentCatalog.structureTreeRoot.parentTree)
                    assertNotNull(pdf.documentInformation.title)
                    val extracted = PDFTextStripper().getText(pdf)
                    if (file == medication) {
                        for (id in 1..120) assertTrue("Missing reminder $id", Regex("QA-$id(?:\\s|$)").containsMatchIn(extracted))
                        assertTrue(extracted.contains(context.getString(com.nullpointer.nourseCompose.R.string.schedule_single_dose)))
                    } else {
                        assertTrue(extracted.contains(context.getString(com.nullpointer.nourseCompose.R.string.pdf_exact_values)))
                    }
                    for (pdfPage in pdf.pages) {
                        assertTrue(pdfPage.structParents >= 0)
                        for (name in pdfPage.resources.fontNames) assertTrue(pdfPage.resources.getFont(name).isEmbedded)
                    }
                }
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        assertTrue(renderer.pageCount > 1)
                        val pages = if (file == health) (0 until renderer.pageCount).toList() else listOf(0, renderer.pageCount - 1)
                        for (index in pages) {
                            renderer.openPage(index).use { page ->
                                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                File(directory, "${file.nameWithoutExtension}-$index.png").outputStream().use {
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                                }
                                bitmap.recycle()
                            }
                        }
                    }
                }
            }
        }
    }

    @Test fun emptyAndSinglePointReportsAreValid() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val measure = MeasureData(1, 98f, null, 1790942400000L, MeasureType.OXYGEN)
        for (values in listOf(emptyList(), listOf(measure), listOf(measure, measure.copy(value1 = Float.NaN)))) {
            val output = java.io.ByteArrayOutputStream()
            HealthDataPdfExporter.write(context, values, emptyList(), output)
            assertTrue(output.toByteArray().take(4).toByteArray().toString(Charsets.US_ASCII) == "%PDF")
        }
    }

    @Test fun callerRetainsOutputStreamOwnership() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = object : java.io.ByteArrayOutputStream() {
            var closed = false
            override fun close() { closed = true; super.close() }
        }
        MedicationReportExporter.write(context, emptyList(), output)
        assertTrue("Exporter closed caller stream", !output.closed)
        assertTrue(output.size() > 0)
    }

    @Test fun medicationPhotosHaveAlternateDescriptions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val photo = File(context.cacheDir, "qa-pdf-photo.png")
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.RED)
        try {
            photo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val reminder = MedicationReminderEntity(name = "Foto de prueba", startAt = 1790942400000L,
                intervalHours = 8, photoUri = android.net.Uri.fromFile(photo).toString())
            val output = java.io.ByteArrayOutputStream()
            MedicationReportExporter.write(context, listOf(reminder), output)
            PDDocument.load(output.toByteArray()).use { pdf ->
                val document = pdf.documentCatalog.structureTreeRoot.kids.single() as
                    com.tom_roush.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement
                val figure = document.kids.filterIsInstance<com.tom_roush.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement>()
                    .single { it.structureType == "Figure" }
                assertTrue(figure.alternateDescription.contains(reminder.name))
            }
        } finally {
            bitmap.recycle()
            photo.delete()
        }
    }
}
