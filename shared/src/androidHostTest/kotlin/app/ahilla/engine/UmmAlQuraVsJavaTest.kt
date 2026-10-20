package app.ahilla.engine

import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Cross-checks our embedded Umm al-Qura table against an independent source: the JDK's built-in
 * `HijrahDate` ("Hijrah-umalqura" variant), which ships its own Umm al-Qura data.
 *
 * Runs on the JVM only (java.time isn't available in commonTest). A mismatch here means the two
 * tables disagree; investigate and decide with the developer — never edit the table to silence it.
 */
class UmmAlQuraVsJavaTest {

    @Test
    fun javaUsesTheUmmAlQuraVariant() {
        assertEquals("Hijrah-umalqura", HijrahChronology.INSTANCE.id)
    }

    @Test
    fun everyDayOfTheTableMatchesJava() {
        val first = UmmAlQuraCalendar.toEpochDay(HijriDate(1343, 1, 1))
        val last = UmmAlQuraCalendar.toEpochDay(HijriDate(1500, 12, UmmAlQuraCalendar.lengthOfMonth(1500, 12)))

        val mismatches = mutableListOf<String>()
        for (epochDay in first..last) {
            val ours = UmmAlQuraCalendar.fromEpochDay(epochDay)
            val java = HijrahDate.from(LocalDate.ofEpochDay(epochDay))
            val theirs = HijriDate(
                java.get(ChronoField.YEAR),
                java.get(ChronoField.MONTH_OF_YEAR),
                java.get(ChronoField.DAY_OF_MONTH),
            )
            if (ours != theirs) mismatches += "${LocalDate.ofEpochDay(epochDay)}: ours $ours, Java $theirs"
        }

        if (mismatches.isNotEmpty()) {
            fail("${mismatches.size} day(s) differ from java.time HijrahDate:\n" + mismatches.joinToString("\n"))
        }
        assertEquals(TabularIslamicCalendar.toEpochDay(HijriDate(1501, 1, 1)), last + 1)
    }
}
