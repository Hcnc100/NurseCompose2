package com.nullpointer.nourseCompose.models.data

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class MeasureValueFormatterTest {
    @Test fun usesSelectedLocaleWithoutRounding() {
        assertEquals("36,56789 °", MeasureValueFormatter.format(36.56789f, null, false, "°", Locale("es", "ES")))
        assertEquals("36.56789 °", MeasureValueFormatter.format(36.56789f, null, false, "°", Locale.US))
    }

    @Test fun removesUnnecessaryDecimalsAndGrouping() {
        assertEquals("120/80 mm Hg", MeasureValueFormatter.format(120f, 80f, true, "mm Hg", Locale.US))
        assertEquals("1000 mg/dL", MeasureValueFormatter.format(1000f, null, false, "mg/dL", Locale.US))
    }

    @Test fun missingAndNonFiniteValuesAreNotRenderedAsNullOrNan() {
        assertEquals("120/— mm Hg", MeasureValueFormatter.format(120f, null, true, "mm Hg", Locale.US))
        assertEquals("— %", MeasureValueFormatter.format(Float.NaN, null, false, "%", Locale.US))
    }
}
