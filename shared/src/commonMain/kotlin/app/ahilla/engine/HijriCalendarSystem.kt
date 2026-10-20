package app.ahilla.engine

/**
 * One way of calculating the Hijri calendar (Umm al-Qura, tabular/civil, …).
 *
 * Every method answers the same few questions, and everything else in the app is built on them:
 * how long is a given month, and which Hijri date corresponds to a given day?
 *
 * Days are exchanged as *epoch days* (days since 1970-01-01, see [GregorianDate]). That single
 * integer is the common currency between calendars, which makes conversions in both directions,
 * user offsets and month-start overrides simple to layer on top.
 *
 * Implementations must be pure: no clock, no locale, no platform APIs, no I/O.
 */
interface HijriCalendarSystem {

    /**
     * Stable identifier, safe to store in settings (e.g. `"umm-al-qura"`).
     * Never rename an id after release: saved preferences depend on it.
     */
    val id: String

    /** Hijri years this method can convert. Outside this range, conversions throw [IllegalArgumentException]. */
    val supportedYears: IntRange

    /** Number of days (29 or 30) in [month] of [year]. */
    fun lengthOfMonth(year: Int, month: Int): Int

    /** Number of days (354 or 355) in [year]. */
    fun lengthOfYear(year: Int): Int = (1..12).sumOf { lengthOfMonth(year, it) }

    /** Epoch day of the given Hijri date. Throws if the date does not exist in this method. */
    fun toEpochDay(date: HijriDate): Long

    /** Hijri date for the given epoch day. Throws if the day is outside [supportedYears]. */
    fun fromEpochDay(epochDay: Long): HijriDate

    /**
     * True if dates in Hijri [year] are an estimate rather than this method's own data,
     * e.g. Umm al-Qura falling back to the tabular rule outside its published table.
     * The UI labels such dates "approx.".
     *
     * It depends only on the year because every data-driven method publishes whole years.
     */
    fun isApproximate(year: Int): Boolean = false

    /** True if [date] exists in this method (in range, and day ≤ length of that month). */
    fun isValid(date: HijriDate): Boolean =
        date.year in supportedYears && date.day <= lengthOfMonth(date.year, date.month)

    fun toGregorian(date: HijriDate): GregorianDate = GregorianDate.fromEpochDay(toEpochDay(date))

    fun toHijri(date: GregorianDate): HijriDate = fromEpochDay(date.toEpochDay())
}
