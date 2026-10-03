package com.nullpointer.nourseCompose.reports

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.os.ConfigurationCompat
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.R
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.cos.*
import com.tom_roush.pdfbox.pdmodel.*
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.documentinterchange.logicalstructure.*
import com.tom_roush.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.graphics.state.RenderingMode
import com.tom_roush.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences
import java.io.Closeable
import java.io.OutputStream
import java.io.FilterOutputStream
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

/** Paginates without truncating records. The caller owns the output stream. */
internal class ReportPdfWriter(private val context: Context) : Closeable {
    private val document = PDDocument()
    private var page: PDPage? = null
    private var stream: PDPageContentStream? = null
    private val root = PDStructureTreeRoot()
    private val structure = PDStructureElement("Document", root)
    private val parentEntries = COSArray()
    private var pageParents = COSArray()
    private var mcid = 0
    private var pageNumber = 0
    private var y = 42f
    val locale: Locale = ConfigurationCompat.getLocales(context.resources.configuration)[0] ?: Locale.getDefault()
    private val dates = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale)
    private val fontPath = "com/tom_roush/pdfbox/resources/ttf/LiberationSans-Regular.ttf"
    private val font: PDType0Font
    private val typeface: Typeface
    private val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = Color.rgb(45, 39, 47) }
    init {
        PDFBoxResourceLoader.init(context.applicationContext)
        font = context.assets.open(fontPath).use { PDType0Font.load(document, it) }
        typeface = Typeface.createFromAsset(context.assets, fontPath)
        body.typeface = typeface
        root.appendKid(structure)
        document.documentCatalog.apply {
            language = locale.toLanguageTag()
            structureTreeRoot = root
            markInfo = PDMarkInfo().apply { isMarked = true }
            viewerPreferences = PDViewerPreferences(COSDictionary()).apply { setDisplayDocTitle(true) }
        }
        document.documentInformation.creator = context.getString(R.string.app_name)
    }
    fun date(time: Long): String = dates.format(Date(time))
    private fun finish() {
        stream?.let {
            it.beginMarkedContent(COSName.ARTIFACT)
            drawText(pageNumber.toString(), 540f, 817f, 10f, false)
            it.endMarkedContent()
            it.close()
        }
        stream = null
        page = null
    }
    private fun ensure(height: Float) {
        if (page == null || y + height > 788f) {
            finish()
            page = PDPage(PDRectangle(595f, 842f)).also {
                it.structParents = pageNumber
                it.cosObject.setName(COSName.getPDFName("Tabs"), "S")
                document.addPage(it)
            }
            parentEntries.add(COSInteger.get(pageNumber.toLong()))
            pageParents = COSArray()
            parentEntries.add(pageParents)
            pageNumber++
            mcid = 0
            stream = PDPageContentStream(document, page)
            stream!!.apply {
                beginMarkedContent(COSName.ARTIFACT)
                setNonStrokingColor(255, 255, 255)
                addRect(0f, 0f, 595f, 842f); fill()
                endMarkedContent()
            }
            y = 42f
        }
    }
    @Suppress("DEPRECATION")
    fun text(value: String, size: Float = 12f, bold: Boolean = false, role: String = if (size >= 24f) "H1" else "P") {
        if (role == "H1" && document.documentInformation.title == null) document.documentInformation.title = value
        val element = PDStructureElement(role, structure).also { structure.appendKid(it) }
        val paint = TextPaint(body).apply { textSize = size }
        val layout = StaticLayout(value, paint, 511, Layout.Alignment.ALIGN_NORMAL, 1f, 3f, false)
        for (line in 0 until layout.lineCount) {
            val height = (layout.getLineBottom(line) - layout.getLineTop(line)).toFloat()
            ensure(height)
            val content = value.substring(layout.getLineStart(line), layout.getLineEnd(line)).trimEnd('\n', '\r')
            tagged(element, content) {
                drawText(content, 42f, y + layout.getLineBaseline(line) - layout.getLineTop(line), size, bold)
            }
            y += height
        }
        y += 7f
    }
    private fun tagged(element: PDStructureElement, actualText: String? = null, draw: () -> Unit) {
        val id = mcid++
        pageParents.add(element.cosObject)
        val reference = COSDictionary().apply {
            setName(COSName.TYPE, "MCR")
            setInt(COSName.MCID, id)
            setItem(COSName.PG, page)
        }
        val kids = (element.cosObject.getDictionaryObject(COSName.K) as? COSArray) ?: COSArray().also { element.cosObject.setItem(COSName.K, it) }
        kids.add(reference)
        val properties = COSDictionary().apply {
            setInt(COSName.MCID, id)
            actualText?.let { setString(COSName.ACTUAL_TEXT, it) }
        }
        stream!!.beginMarkedContent(COSName.getPDFName(element.structureType), PDPropertyList.create(properties))
        try { draw() } finally { stream!!.endMarkedContent() }
    }
    private fun drawText(value: String, x: Float, baseline: Float, size: Float, bold: Boolean) {
        // ActualText retains user Unicode characters even when the font lacks a glyph (e.g. emoji).
        val safe = buildString {
            var index = 0
            while (index < value.length) {
                val cp = Character.codePointAt(value, index)
                val glyph = String(Character.toChars(cp))
                append(if (runCatching { font.encode(glyph) }.isSuccess) glyph else "?")
                index += Character.charCount(cp)
            }
        }
        stream!!.apply {
            setNonStrokingColor(45, 39, 47)
            setStrokingColor(45, 39, 47)
            setLineWidth(.25f)
            beginText()
            setFont(font, size)
            setRenderingMode(if (bold) RenderingMode.FILL_STROKE else RenderingMode.FILL)
            newLineAtOffset(x, 842f - baseline)
            showText(safe)
            endText()
        }
    }
    fun section(value: String, keepWithNext: Float = 70f, role: String = "H2") {
        ensure(keepWithNext)
        y += 8f
        stream!!.apply {
            beginMarkedContent(COSName.ARTIFACT)
            setStrokingColor(179, 71, 102); setLineWidth(2f)
            moveTo(42f, 842f - y); lineTo(553f, 842f - y); stroke()
            endMarkedContent()
        }
        y += 12f
        text(value, 16f, true, role)
    }
    fun photo(bitmap: Bitmap, description: String) {
        ensure(104f)
        val scale = 92f / maxOf(bitmap.width, bitmap.height)
        image(bitmap, 42f, y, bitmap.width * scale, bitmap.height * scale, description)
        y += 104f
    }
    /** Actual timestamp spacing; missing secondary values break the line instead of shifting it. */
    fun chart(values: List<MeasureData>, caption: String) {
        val sorted = values.sortedBy { it.createAt }
        val finite = sorted.flatMap { listOfNotNull(it.value1.takeIf { v -> v.isFinite() }, it.value2?.takeIf { v -> v.isFinite() }) }
        if (finite.isEmpty()) return
        ensure(235f)
        val chartTop = y
        val bitmap = Bitmap.createBitmap(1190, 400, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap).apply { scale(2f, 2f); translate(0f, -chartTop) }
        val left = 90f; val right = 540f; val top = y + 12f; val bottom = y + 156f
        val min = finite.min(); val max = finite.max()
        val padding = ((max.toDouble() - min) * .1).coerceAtLeast(1.0)
        val low = min - padding; val high = max + padding
        val first = sorted.first().createAt.toDouble(); val last = sorted.last().createAt.toDouble()
        fun x(time: Long) = if (first == last) (left + right) / 2 else (left + (time.toDouble() - first) / (last - first) * (right - left)).toFloat()
        fun py(value: Float) = (bottom - (value.toDouble() - low) / (high - low) * (bottom - top)).toFloat()
        val number = NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 1 }
        val axis = Paint(body).apply { textSize = 10f }
        repeat(5) { i ->
            val level = top + (bottom - top) * i / 4
            canvas.drawLine(left, level, right, level, Paint(body).apply { color = Color.LTGRAY; strokeWidth = .5f })
            canvas.drawText(number.format(high - (high - low) * i / 4), 42f, level + 3f, axis)
        }
        fun series(secondary: Boolean) {
            val pen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (secondary) Color.rgb(89, 59, 149) else Color.rgb(155, 42, 78)
                strokeWidth = 2f
                if (secondary) pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
            }
            var previous: PointF? = null
            sorted.forEach { measure ->
                val value = (if (secondary) measure.value2 else measure.value1)?.takeIf { it.isFinite() }
                if (value == null) previous = null else {
                    val point = PointF(x(measure.createAt), py(value))
                    previous?.let { canvas.drawLine(it.x, it.y, point.x, point.y, pen) }
                    if (secondary) canvas.drawRect(point.x - 3, point.y - 3, point.x + 3, point.y + 3, pen)
                    else canvas.drawCircle(point.x, point.y, 3f, pen)
                    previous = point
                }
            }
        }
        series(false)
        if (sorted.any { it.value2 != null }) series(true)
        val shortDate = DateFormat.getDateInstance(DateFormat.SHORT, locale)
        canvas.drawText(shortDate.format(Date(sorted.first().createAt)), left, bottom + 18f, axis)
        val end = shortDate.format(Date(sorted.last().createAt))
        if (first != last) canvas.drawText(end, right - axis.measureText(end), bottom + 18f, axis)
        try {
            image(bitmap, 0f, chartTop, 595f, 200f, context.getString(R.string.pdf_chart_description,
                context.getString(sorted.first().type.titleMeasure), sorted.size, date(sorted.first().createAt), date(sorted.last().createAt)))
        } finally { bitmap.recycle() }
        y = bottom + 30f
        text(caption, 10f)
    }
    private fun image(bitmap: Bitmap, x: Float, top: Float, width: Float, height: Float, description: String) {
        val element = PDStructureElement("Figure", structure).apply { alternateDescription = description }
        structure.appendKid(element)
        tagged(element) { stream!!.drawImage(LosslessFactory.createFromImage(document, bitmap), x, 842f - top - height, width, height) }
    }
    fun write(output: OutputStream) {
        finish()
        root.cosObject.setItem(COSName.PARENT_TREE, COSDictionary().apply { setItem(COSName.NUMS, parentEntries) })
        root.parentTreeNextKey = pageNumber
        // PDFBox closes its writer; never close the caller-owned destination here.
        document.save(object : FilterOutputStream(output) {
            override fun close() = flush()
        })
    }
    override fun close() { finish(); document.close() }
}
