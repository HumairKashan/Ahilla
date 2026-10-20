package app.ahilla.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UmmAlQuraCalendarTest {

    private val cal = UmmAlQuraCalendar

    private fun assertMaps(hijri: HijriDate, gregorian: GregorianDate) {
        assertEquals(gregorian, cal.toGregorian(hijri), "toGregorian($hijri)")
        assertEquals(hijri, cal.toHijri(gregorian), "toHijri($gregorian)")
    }

    // --- Fixed anchors: published Umm al-Qura dates for recent and upcoming observances ---

    @Test
    fun publishedAnchors() {
        assertMaps(HijriDate(1446, 10, 1), GregorianDate(2025, 3, 30))   // Eid al-Fitr 1446
        assertMaps(HijriDate(1447, 9, 1), GregorianDate(2026, 2, 18))    // Ramadan 1447
        assertMaps(HijriDate(1448, 10, 1), GregorianDate(2027, 3, 9))    // Eid al-Fitr 1448
        assertMaps(HijriDate(1448, 12, 1), GregorianDate(2027, 5, 7))    // Dhul Hijjah 1448
    }

    // --- Table integrity: catches copying errors in UmmAlQuraData ---

    @Test
    fun tableArraysCoverTheSameYears() {
        val years = UmmAlQuraData.LAST_YEAR - UmmAlQuraData.FIRST_YEAR + 1
        assertEquals(years, UmmAlQuraData.MONTH_LENGTHS.size)
        assertEquals(years, UmmAlQuraData.YEAR_STARTS.size)
        assertEquals(1343..1500, cal.TABLE_YEARS)
    }

    @Test
    fun yearStartsAgreeWithMonthLengths() {
        for (year in UmmAlQuraData.FIRST_YEAR until UmmAlQuraData.LAST_YEAR) {
            val i = year - UmmAlQuraData.FIRST_YEAR
            assertEquals(
                UmmAlQuraData.YEAR_STARTS[i + 1] - UmmAlQuraData.YEAR_STARTS[i],
                cal.lengthOfYear(year),
                "start of ${year + 1} vs length of $year",
            )
        }
    }

    @Test
    fun monthAndYearLengthsArePlausible() {
        for (year in cal.TABLE_YEARS) {
            assertTrue(cal.lengthOfYear(year) in 354..355, "length of $year")
            for (month in 1..12) assertTrue(cal.lengthOfMonth(year, month) in 29..30, "length of $year-$month")
            // Only the low 12 bits may be used.
            assertEquals(0, UmmAlQuraData.MONTH_LENGTHS[year - UmmAlQuraData.FIRST_YEAR] shr 12, "bits of $year")
        }
    }

    // --- Round trip across the table and both fallback seams ---

    @Test
    fun roundTripsEveryDayFrom1300To1600() {
        val first = TabularIslamicCalendar.toEpochDay(HijriDate(1300, 1, 1))
        val last = TabularIslamicCalendar.toEpochDay(HijriDate(1600, 12, 29))
        var previous: HijriDate? = null
        for (epochDay in first..last) {
            val hijri = cal.fromEpochDay(epochDay)
            assertTrue(cal.isValid(hijri), "$hijri from epoch day $epochDay is not valid")
            assertEquals(epochDay, cal.toEpochDay(hijri), "round trip of $hijri")
            // Consecutive days give strictly increasing Hijri dates: no day is skipped or repeated.
            previous?.let { assertTrue(hijri > it, "$hijri does not follow $it") }
            previous = hijri
        }
    }

    // --- Seams: where the table meets the tabular fallback ---

    @Test
    fun startSeamJoinsTabularWithoutGapOrOverlap() {
        assertMaps(HijriDate(1342, 12, 29), GregorianDate(1924, 8, 1))
        assertMaps(HijriDate(1343, 1, 1), GregorianDate(1924, 8, 2))
        assertEquals(29, cal.lengthOfMonth(1342, 12))
        assertTrue(cal.isValid(HijriDate(1342, 12, 29)))
        assertTrue(cal.isApproximate(1342))
        assertFalse(cal.isValid(HijriDate(1342, 12, 30)))
    }

    @Test
    fun endSeamJoinsTabularWithoutGapOrOverlap() {
        val lastDay = HijriDate(1500, 12, cal.lengthOfMonth(1500, 12))
        assertMaps(lastDay, GregorianDate(2077, 11, 16))
        assertMaps(HijriDate(1501, 1, 1), GregorianDate(2077, 11, 17))
    }

    // --- Boundaries and the approximate flag ---

    @Test
    fun datesInsideTheTableAreExact() {
        for (date in listOf(GregorianDate(1924, 8, 2), GregorianDate(2026, 10, 20), GregorianDate(2077, 11, 16))) {
            assertFalse(cal.isApproximate(cal.toHijri(date).year), "$date should be exact")
        }
    }

    @Test
    fun datesOutsideTheTableAreApproximate() {
        for (date in listOf(GregorianDate(1924, 8, 1), GregorianDate(1900, 1, 1), GregorianDate(2077, 11, 17), GregorianDate(2200, 1, 1))) {
            assertTrue(cal.isApproximate(cal.toHijri(date).year), "$date should be approximate")
        }
        assertTrue(cal.isApproximate(1342))
        assertFalse(cal.isApproximate(1343))
        assertFalse(cal.isApproximate(1500))
        assertTrue(cal.isApproximate(1501))
    }

    @Test
    fun fallbackMatchesTabularExactly() {
        for (date in listOf(HijriDate(1, 1, 1), HijriDate(1000, 9, 1), HijriDate(1342, 12, 29), HijriDate(1501, 1, 1), HijriDate(2001, 12, 30))) {
            assertEquals(TabularIslamicCalendar.toEpochDay(date), cal.toEpochDay(date), "$date")
        }
    }

    @Test
    fun tabularIsNotApproximate() {
        assertFalse(TabularIslamicCalendar.isApproximate(1))
        assertFalse(TabularIslamicCalendar.isApproximate(1600))
    }

    // --- Invalid input ---

    @Test
    fun rejectsDay30OfA29DayTableMonth() {
        // 1343 = 0x0956 = 1001 0101 0110: Safar has 29 days.
        assertEquals(29, cal.lengthOfMonth(1343, 2))
        assertFalse(cal.isValid(HijriDate(1343, 2, 30)))
        assertFailsWith<IllegalArgumentException> { cal.toEpochDay(HijriDate(1343, 2, 30)) }
    }

    @Test
    fun readsBitsInIcuOrder() {
        // 1343 = 1001 0101 0110 → Muharram..Dhu al-Hijjah
        val expected = listOf(30, 29, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29)
        assertEquals(expected, (1..12).map { cal.lengthOfMonth(1343, it) })
    }

    @Test
    fun rejectsDaysBeforeTheHijriEra() {
        assertFailsWith<IllegalArgumentException> { cal.fromEpochDay(TabularIslamicCalendar.EPOCH_DAY - 1) }
    }
}
