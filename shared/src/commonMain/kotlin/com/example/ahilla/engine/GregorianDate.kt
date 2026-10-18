package app.ahilla.engine

/**
 * A calendar date in the proleptic Gregorian calendar (no time, no time zone).
 *
 * The engine deliberately has its own tiny date type instead of depending on a date library:
 * - the engine stays pure Kotlin with zero dependencies, so it behaves identically on Android and iOS;
 * - every calculation goes through a single integer, the [epoch day][toEpochDay], which keeps
 *   all calendar systems easy to compare and test.
 *
 * "Epoch day" = number of days since 1970-01-01 (that day is 0, the day before is -1).
 * This is the same convention as `java.time.LocalDate.toEpochDay()` and
 * `kotlinx.datetime.LocalDate.toEpochDays()`, so converting to/from those in UI code is trivial.
 *
 * @property year Gregorian year (astronomical numbering; 1 BCE is year 0).
 * @property month 1 = January … 12 = December.
 * @property day 1 … length of the month.
 */
data class GregorianDate(val year: Int, val month: Int, val day: Int) : Comparable<GregorianDate> {

    init {
        require(month in 1..12) { "Gregorian month must be 1..12, was $month" }
        require(day in 1..lengthOfMonth(year, month)) {
            "Gregorian day must be 1..${lengthOfMonth(year, month)} for $year-$month, was $day"
        }
    }

    /** Days since 1970-01-01. */
    fun toEpochDay(): Long = daysFromCivil(year.toLong(), month, day)

    /** ISO day of the week. */
    val dayOfWeek: Weekday get() = Weekday.fromEpochDay(toEpochDay())

    fun plusDays(days: Long): GregorianDate = fromEpochDay(toEpochDay() + days)

    override fun compareTo(other: GregorianDate): Int = toEpochDay().compareTo(other.toEpochDay())

    /** ISO-8601 form, e.g. `2026-10-18`. */
    override fun toString(): String =
        "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    companion object {

        fun fromEpochDay(epochDay: Long): GregorianDate = civilFromDays(epochDay)

        fun isLeapYear(year: Int): Boolean =
            (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

        fun lengthOfMonth(year: Int, month: Int): Int = when (month) {
            2 -> if (isLeapYear(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

        // The two functions below are Howard Hinnant's well-known public-domain algorithms
        // ("chrono-Compatible Low-Level Date Algorithms"). They work on "eras" of 400 years
        // (= exactly 146 097 days) and treat March as the first month of the year, so the
        // leap day (29 Feb) falls at the END of the year and needs no special case.

        private fun daysFromCivil(year: Long, month: Int, day: Int): Long {
            val y = if (month <= 2) year - 1 else year          // Jan/Feb belong to the previous "March year"
            val era = y.floorDiv(400L)
            val yearOfEra = y - era * 400                       // 0..399
            val shiftedMonth = if (month > 2) month - 3 else month + 9   // Mar = 0 … Feb = 11
            val dayOfYear = (153 * shiftedMonth + 2) / 5 + day - 1        // 0..365
            val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
            return era * 146_097 + dayOfEra - 719_468           // 719 468 = days from 0000-03-01 to 1970-01-01
        }

        private fun civilFromDays(epochDay: Long): GregorianDate {
            val z = epochDay + 719_468
            val era = z.floorDiv(146_097L)
            val dayOfEra = z - era * 146_097                    // 0..146 096
            val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
            val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
            val shiftedMonth = (5 * dayOfYear + 2) / 153        // Mar = 0 … Feb = 11
            val day = (dayOfYear - (153 * shiftedMonth + 2) / 5 + 1).toInt()
            val month = (if (shiftedMonth < 10) shiftedMonth + 3 else shiftedMonth - 9).toInt()
            val year = (yearOfEra + era * 400 + if (month <= 2) 1 else 0).toInt()
            return GregorianDate(year, month, day)
        }
    }
}

/** ISO weekday: Monday = 1 … Sunday = 7. */
enum class Weekday(val isoNumber: Int) {
    MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6), SUNDAY(7);

    companion object {
        /** 1970-01-01 (epoch day 0) was a Thursday. */
        fun fromEpochDay(epochDay: Long): Weekday = entries[(epochDay + 3).mod(7L).toInt()]
    }
}
