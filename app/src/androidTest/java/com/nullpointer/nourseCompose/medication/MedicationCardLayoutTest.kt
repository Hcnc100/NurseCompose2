package com.nullpointer.nourseCompose.medication

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.ui.screens.medication.MedicationReminderCard
import com.nullpointer.nourseCompose.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@RunWith(Parameterized::class)
class MedicationCardLayoutTest(private val language: String, private val dark: Boolean, private val fontScale: Float) {
    @get:Rule val compose = createComposeRule()
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}-dark={1}-font={2}")
        fun configurations() = listOf("en", "es").flatMap { language ->
            listOf(false, true).flatMap { dark -> listOf(1f, 2f).map { arrayOf<Any>(language, dark, it) } }
        }
    }

    @Test fun measurementTitleAndDateUseSeparateLines() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) }
        val localized = context.createConfigurationContext(configuration)
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides configuration,
                LocalDensity provides Density(density, fontScale)) {
                MyApplicationTheme(darkTheme = dark) {
                    Box(Modifier.width(250.dp)) {
                        com.nullpointer.nourseCompose.ui.share.measureItem.MeasureItem(
                            isSelected = false, isSelectedEnable = false, addMeasureSelected = {},
                            measureData = com.nullpointer.nourseCompose.models.data.MeasureData(
                                id = 1, value1 = 36.5f, value2 = null, createAt = 0,
                                type = com.nullpointer.nourseCompose.models.types.MeasureType.TEMPERATURE,
                            ),
                        )
                    }
                }
            }
        }
        val title = compose.onNodeWithText(localized.getString(com.nullpointer.nourseCompose.R.string.title_temperature), useUnmergedTree = true)
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val datePattern = android.text.format.DateFormat.getBestDateTimePattern(
            Locale.forLanguageTag(language),
            if (android.text.format.DateFormat.is24HourFormat(localized)) "yMdHm" else "yMdhm"
        )
        val date = java.time.Instant.ofEpochMilli(0).atZone(java.time.ZoneId.systemDefault()).format(
            java.time.format.DateTimeFormatter.ofPattern(datePattern, Locale.forLanguageTag(language)))
        val dateNode = compose.onNodeWithText(date, useUnmergedTree = true).assertIsDisplayed()
        assertTrue("Date must be below title", dateNode.fetchSemanticsNode().boundsInRoot.top >= title.bottom)
        val layouts = mutableListOf<TextLayoutResult>()
        dateNode.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val directory = context.getExternalFilesDir("qa-card-layout")!!.apply { mkdirs() }
        File(directory, "measure-$language-$dark-$fontScale.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        val layout = layouts.single()
        assertFalse("Date must not overflow: size=${layout.size}, width=${layout.didOverflowWidth}, height=${layout.didOverflowHeight}, lines=${layout.lineCount}", layout.hasVisualOverflow)
    }

    @Test fun editorLargeTextCanReachDurationAndSaveWithoutClipping() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) }
        val localized = context.createConfigurationContext(configuration)
        compose.setContent {
            val density = LocalDensity.current.density
            val activityResultOwner = requireNotNull(androidx.activity.compose.LocalActivityResultRegistryOwner.current)
            CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides configuration,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides activityResultOwner,
                LocalDensity provides Density(density, fontScale)) {
                MyApplicationTheme(darkTheme = dark) {
                    com.nullpointer.nourseCompose.ui.screens.medication.MedicationReminderEditor(null, {}, {})
                }
            }
        }
        fun assertTextFits(id: Int, scroll: Boolean = false) {
            val node = compose.onNodeWithText(localized.getString(id), useUnmergedTree = true)
            if (scroll) node.performScrollTo()
            node.assertIsDisplayed()
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue("Text must be measured: $id", layouts.isNotEmpty())
            assertTrue("Text must not overflow: ${localized.getString(id)} ${layouts.map { "size=${it.size}, lines=${it.lineCount}, width=${it.didOverflowWidth}, height=${it.didOverflowHeight}" }}", layouts.none { it.hasVisualOverflow })
        }
        val initialDirectory = context.getExternalFilesDir("qa-card-layout")!!.apply { mkdirs() }
        File(initialDirectory, "editor-top-$language-$dark-$fontScale.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        assertTextFits(com.nullpointer.nourseCompose.R.string.title_add_medication)
        assertTextFits(com.nullpointer.nourseCompose.R.string.action_save)
        assertTextFits(com.nullpointer.nourseCompose.R.string.schedule_indefinite, scroll = true)
        assertTextFits(com.nullpointer.nourseCompose.R.string.action_save)
        val directory = context.getExternalFilesDir("qa-card-layout")!!.apply { mkdirs() }
        File(directory, "editor-$language-$dark-$fontScale.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText(localized.getString(com.nullpointer.nourseCompose.R.string.schedule_single_dose))
            .performScrollTo().performClick()
        compose.onNodeWithText(localized.getString(com.nullpointer.nourseCompose.R.string.label_interval_hours)).assertDoesNotExist()
        compose.onNodeWithText(localized.getString(com.nullpointer.nourseCompose.R.string.label_next_doses)).assertDoesNotExist()
        assertTextFits(com.nullpointer.nourseCompose.R.string.action_save)
        File(directory, "editor-single-$language-$dark-$fontScale.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText(localized.getString(com.nullpointer.nourseCompose.R.string.schedule_date_range))
            .performScrollTo().performClick()
        compose.onNodeWithText(localized.getString(com.nullpointer.nourseCompose.R.string.label_end_date), substring = true)
            .performScrollTo().assertIsDisplayed()
        assertTextFits(com.nullpointer.nourseCompose.R.string.action_save)
        File(directory, "editor-range-$language-$dark-$fontScale.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test fun nextAlarmFitsCardWithLargeTextAndTranslations() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = Locale.forLanguageTag(language)
        val configuration = Configuration(context.resources.configuration).apply { setLocale(locale) }
        val localized = context.createConfigurationContext(configuration)
        val at = 864_000_000L
        val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(Date(at))
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides configuration,
                LocalDensity provides Density(density, fontScale)) {
                MyApplicationTheme(darkTheme = dark) {
                    Box(Modifier.width(320.dp).testTag("card-host")) {
                        MedicationReminderCard(MedicationReminderEntity(name = "Test medication", dosage = "One tablet", startAt = at, intervalHours = 1),
                            now = 0, scheduledAt = at, onClick = {}, onActiveChange = {}, onDelete = {})
                    }
                }
            }
        }
        val node = compose.onNodeWithText(date, useUnmergedTree = true).assertIsDisplayed()
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue("Text layout was exposed", layouts.isNotEmpty())
        assertFalse("Date must wrap without overflowing", layouts.first().hasVisualOverflow)
        val host = compose.onNodeWithTag("card-host").fetchSemanticsNode().boundsInRoot
        val bounds = node.fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.left >= host.left && bounds.right <= host.right)
        val directory = context.getExternalFilesDir("qa-card-layout")!!.apply { mkdirs() }
        File(directory, "$language-$dark-$fontScale.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
