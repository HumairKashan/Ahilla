package app.ahilla.engine

/**
 * The tabular ("arithmetical" or "civil") Islamic calendar.
 *
 * Instead of observing or computing the moon, it follows a fixed rule:
 * - months alternate 30 and 29 days (Muharram 30, Safar 29, …), so a normal year has 354 days;
 * - in a 30-year cycle, 11 years are leap years in which Dhu al-Hijjah gets a 30th day (355 days).
 *   The leap years are 2, 5, 7, 10, 13, 16, 18, 21, 24, 26 and 29 of each cycle (the most common
 *   variant, used e.g. by ICU's "islamic-civil" and Microsoft's HijriCalendar);
 * - day 1 (1 Muharram 1 AH) is Friday 16 July 622 in the Julian calendar,
 *   which is 19 July 622 in the proleptic Gregorian calendar (the "civil" or Friday epoch).
 *
 * It is predictable forever and needs no data, but it can drift a day or two from the real
 * crescent. That's why Umm al-Qura is the app's default and this method is the alternative.
 */
object TabularIslamicCalendar : HijriCalendarSystem {

    override val id: String = "tabular-civil"

    /** The rule works for any year; we cap it at a range no user will ever reach. */
    override val supportedYears: IntRange = 1..9999

    /** Epoch day of 1 Muharram 1 AH = Gregorian 0622-07-19 (Julian Day Number 1 948 440). */
    internal const val EPOCH_DAY: Long = -492_148L

    /** True if Dhu al-Hijjah of [year] has 30 days. Equivalent to the leap-year list in the class docs. */
    fun isLeapYear(year: Int): Boolean = (14 + 11 * year).mod(30) < 11

    override fun lengthOfMonth(year: Int, month: Int): Int {
        requireYear(year)
        require(month in 1..12) { "Hijri month must be 1..12, was $month" }
        return when {
            month == 12 && isLeapYear(year) -> 30
            month % 2 == 1 -> 30
            else -> 29
        }
    }

    override fun toEpochDay(date: HijriDate): Long {
        require(isValid(date)) { "$date does not exist in the tabular calendar" }
        return EPOCH_DAY + daysBeforeYear(date.year) + daysBeforeMonth(date.month) + (date.day - 1)
    }

    override fun fromEpochDay(epochDay: Long): HijriDate {
        val daysSinceEpoch = epochDay - EPOCH_DAY
        require(daysSinceEpoch >= 0) { "Epoch day $epochDay is before 1 Muharram 1 AH" }

        // A tabular year averages 10 631 / 30 days, so this estimate is right or off by one.
        // The two loops correct it; they run at most once each.
        var year = ((30 * daysSinceEpoch + 10_646) / 10_631).toInt()
        while (year > 1 && daysBeforeYear(year) > daysSinceEpoch) year--
        while (daysBeforeYear(year + 1) <= daysSinceEpoch) year++
        requireYear(year)

        val dayOfYear = (daysSinceEpoch - daysBeforeYear(year)).toInt()   // 0-based
        var month = 12
        while (daysBeforeMonth(month) > dayOfYear) month--
        val day = dayOfYear - daysBeforeMonth(month) + 1
        return HijriDate(year, month, day)
    }

    /**
     * Days from 1 Muharram 1 AH to 1 Muharram of [year].
     * 354 days per elapsed year, plus one for each leap year that has passed:
     * `floor((3 + 11·year) / 30)` counts exactly the leap years among 1 … year-1.
     */
    private fun daysBeforeYear(year: Int): Long = 354L * (year - 1) + (3 + 11L * year).floorDiv(30L)

    /** Days from 1 Muharram to the 1st of [month]: months alternate 30/29, i.e. ceil(29.5 × (month − 1)). */
    private fun daysBeforeMonth(month: Int): Int = (59 * (month - 1) + 1) / 2

    private fun requireYear(year: Int) =
        require(year in supportedYears) { "Hijri year $year is outside the supported range $supportedYears" }
}
