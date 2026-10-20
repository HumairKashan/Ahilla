package app.ahilla.engine

/**
 * A date in the Hijri (Islamic lunar) calendar.
 *
 * This type only checks what is true for *every* Hijri calculation method (month 1..12, day 1..30).
 * Whether a particular day 30 actually exists depends on the method — Umm al-Qura and the tabular
 * calendar can disagree on whether a month has 29 or 30 days — so use
 * [HijriCalendarSystem.isValid] or [HijriCalendarSystem.lengthOfMonth] for that.
 *
 * @property year Hijri year (AH), 1 or later.
 * @property month 1 = Muharram … 12 = Dhu al-Hijjah. See [HijriMonth].
 * @property day 1 … 29 or 30.
 */
data class HijriDate(val year: Int, val month: Int, val day: Int) : Comparable<HijriDate> {

    init {
        require(year >= 1) { "Hijri year must be 1 or later, was $year" }
        require(month in 1..12) { "Hijri month must be 1..12, was $month" }
        require(day in 1..30) { "Hijri day must be 1..30, was $day" }
    }

    val hijriMonth: HijriMonth get() = HijriMonth.of(month)

    // Comparing field by field is valid in every method: within a year months run 1..12 and days 1..30.
    override fun compareTo(other: HijriDate): Int = compareValuesBy(this, other, { it.year }, { it.month }, { it.day })

    /** Machine-friendly form, e.g. `1448-09-01`. Not for display; the UI formats dates itself. */
    override fun toString(): String =
        "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

/**
 * The twelve Hijri months.
 *
 * The names here are stable reference names for code, logs and tests. Text shown to users will come
 * from localised string resources later (English, Arabic, Urdu, Malay…), not from this enum.
 */
enum class HijriMonth(val number: Int, val referenceName: String) {
    MUHARRAM(1, "Muharram"),
    SAFAR(2, "Safar"),
    RABI_AL_AWWAL(3, "Rabi' al-Awwal"),
    RABI_AL_THANI(4, "Rabi' al-Thani"),
    JUMADA_AL_ULA(5, "Jumada al-Ula"),
    JUMADA_AL_AKHIRAH(6, "Jumada al-Akhirah"),
    RAJAB(7, "Rajab"),
    SHABAN(8, "Sha'ban"),
    RAMADAN(9, "Ramadan"),
    SHAWWAL(10, "Shawwal"),
    DHU_AL_QADAH(11, "Dhu al-Qa'dah"),
    DHU_AL_HIJJAH(12, "Dhu al-Hijjah");

    companion object {
        fun of(number: Int): HijriMonth {
            require(number in 1..12) { "Hijri month must be 1..12, was $number" }
            return entries[number - 1]
        }
    }
}
