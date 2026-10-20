package app.ahilla.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GregorianDateTest {

    @Test
    fun epochDayAnchors() {
        // Values match java.time.LocalDate.toEpochDay().
        assertEquals(0L, GregorianDate(1970, 1, 1).toEpochDay())
        assertEquals(-1L, GregorianDate(1969, 12, 31).toEpochDay())
        assertEquals(10_957L, GregorianDate(2000, 1, 1).toEpochDay())
        assertEquals(11_016L, GregorianDate(2000, 2, 29).toEpochDay())
        assertEquals(20_744L, GregorianDate(2026, 10, 18).toEpochDay())
        assertEquals(-492_148L, GregorianDate(622, 7, 19).toEpochDay())
    }

    @Test
    fun roundTripEveryDayFrom600To2200() {
        val start = GregorianDate(600, 1, 1).toEpochDay()
        val end = GregorianDate(2200, 12, 31).toEpochDay()
        var expected = GregorianDate(600, 1, 1)
        for (epochDay in start..end) {
            val date = GregorianDate.fromEpochDay(epochDay)
            assertEquals(expected, date, "epoch day $epochDay")
            assertEquals(epochDay, date.toEpochDay())
            expected = nextDay(expected)
        }
    }

    @Test
    fun leapYears() {
        assertTrue(GregorianDate.isLeapYear(2000))   // divisible by 400
        assertFalse(GregorianDate.isLeapYear(1900))  // divisible by 100 but not 400
        assertTrue(GregorianDate.isLeapYear(2028))
        assertFalse(GregorianDate.isLeapYear(2026))
        assertEquals(29, GregorianDate.lengthOfMonth(2028, 2))
        assertEquals(28, GregorianDate.lengthOfMonth(2027, 2))
    }

    @Test
    fun weekdays() {
        assertEquals(Weekday.THURSDAY, GregorianDate(1970, 1, 1).dayOfWeek)
        assertEquals(Weekday.SUNDAY, GregorianDate(2026, 10, 18).dayOfWeek)
        // 1 Muharram 1 AH (civil epoch) was a Friday: Julian 16 July 622 = Gregorian 19 July 622.
        assertEquals(Weekday.FRIDAY, GregorianDate(622, 7, 19).dayOfWeek)
        assertEquals(Weekday.MONDAY, GregorianDate(1969, 12, 29).dayOfWeek) // negative epoch days
    }

    @Test
    fun rejectsImpossibleDates() {
        assertFailsWith<IllegalArgumentException> { GregorianDate(2026, 2, 29) }
        assertFailsWith<IllegalArgumentException> { GregorianDate(2026, 13, 1) }
        assertFailsWith<IllegalArgumentException> { GregorianDate(2026, 4, 31) }
    }

    @Test
    fun formatting() {
        assertEquals("2026-10-18", GregorianDate(2026, 10, 18).toString())
        assertEquals("0622-07-19", GregorianDate(622, 7, 19).toString())
    }

    /** Naive "next day" written independently of the epoch-day algorithm, used as a cross-check. */
    private fun nextDay(d: GregorianDate): GregorianDate = when {
        d.day < GregorianDate.lengthOfMonth(d.year, d.month) -> GregorianDate(d.year, d.month, d.day + 1)
        d.month < 12 -> GregorianDate(d.year, d.month + 1, 1)
        else -> GregorianDate(d.year + 1, 1, 1)
    }
}
