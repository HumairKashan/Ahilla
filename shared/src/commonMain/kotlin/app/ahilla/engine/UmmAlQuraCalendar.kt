package app.ahilla.engine

/**
 * The Umm al-Qura calendar, the official calendar of Saudi Arabia and the app's default method.
 *
 * Umm al-Qura month starts are published as a table, not a rule, so this uses our embedded copy
 * ([UmmAlQuraData], 1343–1500 AH). Outside [TABLE_YEARS] it falls back to the
 * [tabular calendar][TabularIslamicCalendar], and [isApproximate] returns true for those years.
 *
 * The two methods join without a gap or overlap at both ends: tabular 1342 ends on 1 Aug 1924, the
 * day before Umm al-Qura's 1 Muharram 1343 (2 Aug 1924); Umm al-Qura 1500 ends on 16 Nov 2077, the
 * day before tabular's 1 Muharram 1501 (17 Nov 2077). So every day maps to exactly one Hijri date.
 * The unit tests pin both seams.
 */
object UmmAlQuraCalendar : HijriCalendarSystem {

    override val id: String = "umm-al-qura"

    /** Years covered by the embedded table. Other years are tabular estimates. */
    val TABLE_YEARS: IntRange = UmmAlQuraData.FIRST_YEAR..UmmAlQuraData.LAST_YEAR

    /** Same as the tabular fallback, so conversions never fail just because a year is off the table. */
    override val supportedYears: IntRange = TabularIslamicCalendar.supportedYears

    /** First epoch day after the table (1 Muharram 1501). */
    private val tableEndEpochDay: Long =
        UmmAlQuraData.YEAR_STARTS.last().toLong() + tableYearLength(UmmAlQuraData.LAST_YEAR)

    override fun isApproximate(year: Int): Boolean = year !in TABLE_YEARS

    override fun lengthOfMonth(year: Int, month: Int): Int {
        if (year !in TABLE_YEARS) return TabularIslamicCalendar.lengthOfMonth(year, month)
        require(month in 1..12) { "Hijri month must be 1..12, was $month" }
        return tableMonthLength(year, month)
    }

    override fun toEpochDay(date: HijriDate): Long {
        if (date.year !in TABLE_YEARS) return TabularIslamicCalendar.toEpochDay(date)
        require(isValid(date)) { "$date does not exist in the Umm al-Qura calendar" }
        var epochDay = yearStart(date.year)
        for (month in 1 until date.month) epochDay += tableMonthLength(date.year, month)
        return epochDay + (date.day - 1)
    }

    override fun fromEpochDay(epochDay: Long): HijriDate {
        if (epochDay < yearStart(UmmAlQuraData.FIRST_YEAR) || epochDay >= tableEndEpochDay) {
            return TabularIslamicCalendar.fromEpochDay(epochDay)
        }
        val year = UmmAlQuraData.FIRST_YEAR + lastYearStartingOnOrBefore(epochDay)
        var dayOfYear = (epochDay - yearStart(year)).toInt()   // 0-based
        var month = 1
        while (dayOfYear >= tableMonthLength(year, month)) {
            dayOfYear -= tableMonthLength(year, month)
            month++
        }
        return HijriDate(year, month, dayOfYear + 1)
    }

    /** Index into [UmmAlQuraData.YEAR_STARTS] of the year containing [epochDay] (binary search). */
    private fun lastYearStartingOnOrBefore(epochDay: Long): Int {
        val starts = UmmAlQuraData.YEAR_STARTS
        var low = 0
        var high = starts.size - 1
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (starts[mid] <= epochDay) low = mid else high = mid - 1
        }
        return low
    }

    private fun yearStart(year: Int): Long = UmmAlQuraData.YEAR_STARTS[year - UmmAlQuraData.FIRST_YEAR].toLong()

    /** Reads ICU's bit layout: bit 11 = Muharram … bit 0 = Dhu al-Hijjah; set = 30 days. */
    private fun tableMonthLength(year: Int, month: Int): Int {
        val bits = UmmAlQuraData.MONTH_LENGTHS[year - UmmAlQuraData.FIRST_YEAR]
        return if ((bits shr (12 - month)) and 1 == 1) 30 else 29
    }

    private fun tableYearLength(year: Int): Int = (1..12).sumOf { tableMonthLength(year, it) }
}
