package app.ahilla.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TabularIslamicCalendarTest {

    private val cal = TabularIslamicCalendar

    private data class Case(val hijri: HijriDate, val gregorian: GregorianDate)

    /**
     * Reference dates from an independent implementation: John Walker's (Fourmilab) tabular
     * Islamic calendar, as packaged in the Python library `convertdate` (islamic module).
     * Covers the epoch, month starts of Ramadan/Shawwal/Dhu al-Hijjah across 1 600 years,
     * the last day of leap and common years, and random dates.
     */
    private val referenceDates = listOf(
        Case(HijriDate(1, 1, 1), GregorianDate(622, 7, 19)),
        Case(HijriDate(1, 9, 1), GregorianDate(623, 3, 12)),
        Case(HijriDate(1, 10, 1), GregorianDate(623, 4, 11)),
        Case(HijriDate(1, 12, 1), GregorianDate(623, 6, 9)),
        Case(HijriDate(2, 1, 1), GregorianDate(623, 7, 8)),
        Case(HijriDate(2, 9, 1), GregorianDate(624, 2, 29)),
        Case(HijriDate(2, 10, 1), GregorianDate(624, 3, 30)),
        Case(HijriDate(2, 12, 1), GregorianDate(624, 5, 28)),
        Case(HijriDate(100, 1, 1), GregorianDate(718, 8, 7)),
        Case(HijriDate(100, 9, 1), GregorianDate(719, 3, 31)),
        Case(HijriDate(100, 10, 1), GregorianDate(719, 4, 30)),
        Case(HijriDate(100, 12, 1), GregorianDate(719, 6, 28)),
        Case(HijriDate(105, 4, 25), GregorianDate(723, 10, 5)),
        Case(HijriDate(218, 8, 26), GregorianDate(833, 9, 20)),
        Case(HijriDate(266, 6, 10), GregorianDate(880, 1, 31)),
        Case(HijriDate(500, 1, 1), GregorianDate(1106, 9, 9)),
        Case(HijriDate(500, 9, 1), GregorianDate(1107, 5, 3)),
        Case(HijriDate(500, 10, 1), GregorianDate(1107, 6, 2)),
        Case(HijriDate(500, 12, 1), GregorianDate(1107, 7, 31)),
        Case(HijriDate(615, 12, 2), GregorianDate(1219, 2, 26)),
        Case(HijriDate(896, 12, 7), GregorianDate(1491, 10, 20)),
        Case(HijriDate(1000, 1, 1), GregorianDate(1591, 10, 19)),
        Case(HijriDate(1000, 9, 1), GregorianDate(1592, 6, 11)),
        Case(HijriDate(1000, 10, 1), GregorianDate(1592, 7, 11)),
        Case(HijriDate(1000, 12, 1), GregorianDate(1592, 9, 8)),
        Case(HijriDate(1255, 3, 9), GregorianDate(1839, 5, 23)),
        Case(HijriDate(1285, 5, 18), GregorianDate(1868, 9, 6)),
        Case(HijriDate(1300, 1, 1), GregorianDate(1882, 11, 12)),
        Case(HijriDate(1300, 9, 1), GregorianDate(1883, 7, 6)),
        Case(HijriDate(1300, 10, 1), GregorianDate(1883, 8, 5)),
        Case(HijriDate(1300, 12, 1), GregorianDate(1883, 10, 3)),
        Case(HijriDate(1339, 8, 16), GregorianDate(1921, 4, 25)),
        Case(HijriDate(1356, 1, 1), GregorianDate(1937, 3, 14)),
        Case(HijriDate(1356, 9, 1), GregorianDate(1937, 11, 5)),
        Case(HijriDate(1356, 10, 1), GregorianDate(1937, 12, 5)),
        Case(HijriDate(1356, 12, 1), GregorianDate(1938, 2, 2)),
        Case(HijriDate(1393, 7, 1), GregorianDate(1973, 7, 31)),
        Case(HijriDate(1400, 1, 1), GregorianDate(1979, 11, 21)),
        Case(HijriDate(1400, 9, 1), GregorianDate(1980, 7, 14)),
        Case(HijriDate(1400, 10, 1), GregorianDate(1980, 8, 13)),
        Case(HijriDate(1400, 12, 1), GregorianDate(1980, 10, 11)),
        Case(HijriDate(1420, 1, 1), GregorianDate(1999, 4, 17)),
        Case(HijriDate(1420, 9, 1), GregorianDate(1999, 12, 9)),
        Case(HijriDate(1420, 10, 1), GregorianDate(2000, 1, 8)),
        Case(HijriDate(1420, 12, 1), GregorianDate(2000, 3, 7)),
        Case(HijriDate(1425, 1, 1), GregorianDate(2004, 2, 22)),
        Case(HijriDate(1425, 9, 1), GregorianDate(2004, 10, 15)),
        Case(HijriDate(1425, 10, 1), GregorianDate(2004, 11, 14)),
        Case(HijriDate(1425, 12, 1), GregorianDate(2005, 1, 12)),
        Case(HijriDate(1440, 1, 1), GregorianDate(2018, 9, 12)),
        Case(HijriDate(1440, 9, 1), GregorianDate(2019, 5, 6)),
        Case(HijriDate(1440, 10, 1), GregorianDate(2019, 6, 5)),
        Case(HijriDate(1440, 12, 1), GregorianDate(2019, 8, 3)),
        Case(HijriDate(1445, 1, 1), GregorianDate(2023, 7, 19)),
        Case(HijriDate(1445, 8, 29), GregorianDate(2024, 3, 10)),
        Case(HijriDate(1445, 9, 1), GregorianDate(2024, 3, 11)),
        Case(HijriDate(1445, 9, 30), GregorianDate(2024, 4, 9)),
        Case(HijriDate(1445, 10, 1), GregorianDate(2024, 4, 10)),
        Case(HijriDate(1445, 12, 1), GregorianDate(2024, 6, 8)),
        Case(HijriDate(1445, 12, 30), GregorianDate(2024, 7, 7)),
        Case(HijriDate(1446, 1, 1), GregorianDate(2024, 7, 8)),
        Case(HijriDate(1446, 9, 1), GregorianDate(2025, 3, 1)),
        Case(HijriDate(1446, 10, 1), GregorianDate(2025, 3, 31)),
        Case(HijriDate(1446, 12, 1), GregorianDate(2025, 5, 29)),
        Case(HijriDate(1447, 1, 1), GregorianDate(2025, 6, 27)),
        Case(HijriDate(1447, 8, 29), GregorianDate(2026, 2, 17)),
        Case(HijriDate(1447, 9, 1), GregorianDate(2026, 2, 18)),
        Case(HijriDate(1447, 9, 30), GregorianDate(2026, 3, 19)),
        Case(HijriDate(1447, 10, 1), GregorianDate(2026, 3, 20)),
        Case(HijriDate(1447, 12, 1), GregorianDate(2026, 5, 18)),
        Case(HijriDate(1447, 12, 30), GregorianDate(2026, 6, 16)),
        Case(HijriDate(1448, 1, 1), GregorianDate(2026, 6, 17)),
        Case(HijriDate(1448, 8, 29), GregorianDate(2027, 2, 7)),
        Case(HijriDate(1448, 9, 1), GregorianDate(2027, 2, 8)),
        Case(HijriDate(1448, 9, 30), GregorianDate(2027, 3, 9)),
        Case(HijriDate(1448, 10, 1), GregorianDate(2027, 3, 10)),
        Case(HijriDate(1448, 12, 1), GregorianDate(2027, 5, 8)),
        Case(HijriDate(1448, 12, 29), GregorianDate(2027, 6, 5)),
        Case(HijriDate(1449, 1, 1), GregorianDate(2027, 6, 6)),
        Case(HijriDate(1449, 9, 1), GregorianDate(2028, 1, 28)),
        Case(HijriDate(1449, 10, 1), GregorianDate(2028, 2, 27)),
        Case(HijriDate(1449, 12, 1), GregorianDate(2028, 4, 26)),
        Case(HijriDate(1450, 1, 1), GregorianDate(2028, 5, 25)),
        Case(HijriDate(1450, 8, 29), GregorianDate(2029, 1, 15)),
        Case(HijriDate(1450, 9, 1), GregorianDate(2029, 1, 16)),
        Case(HijriDate(1450, 9, 30), GregorianDate(2029, 2, 14)),
        Case(HijriDate(1450, 10, 1), GregorianDate(2029, 2, 15)),
        Case(HijriDate(1450, 12, 1), GregorianDate(2029, 4, 15)),
        Case(HijriDate(1450, 12, 30), GregorianDate(2029, 5, 14)),
        Case(HijriDate(1500, 1, 1), GregorianDate(2076, 11, 28)),
        Case(HijriDate(1500, 9, 1), GregorianDate(2077, 7, 22)),
        Case(HijriDate(1500, 10, 1), GregorianDate(2077, 8, 21)),
        Case(HijriDate(1500, 12, 1), GregorianDate(2077, 10, 19)),
        Case(HijriDate(1563, 10, 2), GregorianDate(2138, 10, 7)),
        Case(HijriDate(1572, 7, 4), GregorianDate(2147, 4, 5)),
        Case(HijriDate(1572, 9, 6), GregorianDate(2147, 6, 5)),
        Case(HijriDate(1600, 1, 1), GregorianDate(2173, 12, 6)),
        Case(HijriDate(1600, 9, 1), GregorianDate(2174, 7, 30)),
        Case(HijriDate(1600, 10, 1), GregorianDate(2174, 8, 29)),
        Case(HijriDate(1600, 12, 1), GregorianDate(2174, 10, 27)),
    )

    @Test
    fun matchesReferenceDatesInBothDirections() {
        for ((hijri, gregorian) in referenceDates) {
            assertEquals(gregorian, cal.toGregorian(hijri), "Hijri → Gregorian for $hijri")
            assertEquals(hijri, cal.toHijri(gregorian), "Gregorian → Hijri for $gregorian")
        }
    }

    @Test
    fun epochIsFriday19July622() {
        val first = HijriDate(1, 1, 1)
        assertEquals(GregorianDate(622, 7, 19), cal.toGregorian(first))
        assertEquals(Weekday.FRIDAY, cal.toGregorian(first).dayOfWeek)
    }

    @Test
    fun leapYearsFollowThe30YearCycle() {
        val leapYearsInFirstCycle = (1..30).filter { cal.isLeapYear(it) }
        assertEquals(listOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29), leapYearsInFirstCycle)
        // The pattern repeats every 30 years.
        for (year in 1..1500) assertEquals(cal.isLeapYear(year), cal.isLeapYear(year + 30), "year $year")
    }

    @Test
    fun monthAndYearLengths() {
        assertEquals(30, cal.lengthOfMonth(1447, 1))   // Muharram
        assertEquals(29, cal.lengthOfMonth(1447, 2))   // Safar
        assertEquals(30, cal.lengthOfMonth(1447, 9))   // Ramadan
        assertEquals(29, cal.lengthOfMonth(1447, 10))  // Shawwal
        assertEquals(30, cal.lengthOfMonth(1447, 12))  // 1447 is a leap year
        assertEquals(29, cal.lengthOfMonth(1448, 12))  // 1448 is not
        assertEquals(355, cal.lengthOfYear(1447))
        assertEquals(354, cal.lengthOfYear(1448))
        // A full 30-year cycle is exactly 10 631 days.
        assertEquals(10_631, (1..30).sumOf { cal.lengthOfYear(it) })
    }

    /**
     * Walks every single day from 1 Muharram 1 AH to the end of 1600 AH and checks that:
     * each day maps to the next Hijri date in sequence, and converting back gives the same day.
     * This catches off-by-one errors at every month and year boundary, not just the sampled ones.
     */
    @Test
    fun everyDayIsContinuousAndRoundTrips() {
        val start = cal.toEpochDay(HijriDate(1, 1, 1))
        val end = cal.toEpochDay(HijriDate(1600, 12, cal.lengthOfMonth(1600, 12)))
        var expected = HijriDate(1, 1, 1)
        for (epochDay in start..end) {
            val hijri = cal.fromEpochDay(epochDay)
            assertEquals(expected, hijri, "epoch day $epochDay")
            assertEquals(epochDay, cal.toEpochDay(hijri))
            expected = nextDay(hijri)
        }
    }

    @Test
    fun rejectsDatesThatDoNotExist() {
        assertFalse(cal.isValid(HijriDate(1448, 12, 30)))   // not a leap year
        assertTrue(cal.isValid(HijriDate(1447, 12, 30)))
        assertFailsWith<IllegalArgumentException> { cal.toGregorian(HijriDate(1448, 2, 30)) }
        assertFailsWith<IllegalArgumentException> { cal.toHijri(GregorianDate(622, 7, 18)) } // before 1 AH
        assertFailsWith<IllegalArgumentException> { HijriDate(1448, 13, 1) }
        assertFailsWith<IllegalArgumentException> { HijriDate(0, 1, 1) }
    }

    private fun nextDay(d: HijriDate): HijriDate = when {
        d.day < cal.lengthOfMonth(d.year, d.month) -> HijriDate(d.year, d.month, d.day + 1)
        d.month < 12 -> HijriDate(d.year, d.month + 1, 1)
        else -> HijriDate(d.year + 1, 1, 1)
    }
}
