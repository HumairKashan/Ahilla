package app.ahilla.engine

/**
 * Embedded Umm al-Qura data for 1343–1500 AH, used by [UmmAlQuraCalendar].
 *
 * Source: ICU 78.3 (git tag `release-78.3`), file `icu4c/source/i18n/islamcal.cpp`,
 * https://github.com/unicode-org/icu. © 2016 and later: Unicode, Inc. and others.
 * Licensed under the Unicode License v3; see THIRD_PARTY_NOTICES.md at the repository root.
 *
 * We embed our own copy instead of calling the platform (java.time on Android, Foundation on iOS)
 * so both apps always show exactly the same dates, whatever the OS version ships.
 */
internal object UmmAlQuraData {

    const val FIRST_YEAR: Int = 1343
    const val LAST_YEAR: Int = 1500

    /**
     * Month lengths, one entry per year from [FIRST_YEAR], copied verbatim from ICU's
     * `UMALQURA_MONTHLENGTH` so the values can be diffed against the source.
     *
     * Each entry uses 12 bits: bit 11 (the highest) is Muharram … bit 0 is Dhu al-Hijjah.
     * A set bit means the month has 30 days, a clear bit 29. Example: 0x0956 = 1001 0101 0110.
     */
    val MONTH_LENGTHS: IntArray = intArrayOf(
        /* 1343-1347 */ 0x0956, 0x0ACA, 0x0BA4, 0x0BD2, 0x05D9,
        /* 1348-1352 */ 0x02DC, 0x096D, 0x054D, 0x0AA5, 0x0B52,
        /* 1353-1357 */ 0x0BA5, 0x05B4, 0x09B6, 0x0557, 0x0297,
        /* 1358-1362 */ 0x054B, 0x06A3, 0x0752, 0x0B65, 0x056A,
        /* 1363-1367 */ 0x0AAB, 0x052B, 0x0C95, 0x0D4A, 0x0DA5,
        /* 1368-1372 */ 0x05CA, 0x0AD6, 0x0957, 0x04AB, 0x094B,
        /* 1373-1377 */ 0x0AA5, 0x0B52, 0x0B6A, 0x0575, 0x0276,
        /* 1378-1382 */ 0x08B7, 0x045B, 0x0555, 0x05A9, 0x05B4,
        /* 1383-1387 */ 0x09DA, 0x04DD, 0x026E, 0x0936, 0x0AAA,
        /* 1388-1392 */ 0x0D54, 0x0DB2, 0x05D5, 0x02DA, 0x095B,
        /* 1393-1397 */ 0x04AB, 0x0A55, 0x0B49, 0x0B64, 0x0B71,
        /* 1398-1402 */ 0x05B4, 0x0AB5, 0x0A55, 0x0D25, 0x0E92,
        /* 1403-1407 */ 0x0EC9, 0x06D4, 0x0AE9, 0x096B, 0x04AB,
        /* 1408-1412 */ 0x0A93, 0x0D49, 0x0DA4, 0x0DB2, 0x0AB9,
        /* 1413-1417 */ 0x04BA, 0x0A5B, 0x052B, 0x0A95, 0x0B2A,
        /* 1418-1422 */ 0x0B55, 0x055C, 0x04BD, 0x023D, 0x091D,
        /* 1423-1427 */ 0x0A95, 0x0B4A, 0x0B5A, 0x056D, 0x02B6,
        /* 1428-1432 */ 0x093B, 0x049B, 0x0655, 0x06A9, 0x0754,
        /* 1433-1437 */ 0x0B6A, 0x056C, 0x0AAD, 0x0555, 0x0B29,
        /* 1438-1442 */ 0x0B92, 0x0BA9, 0x05D4, 0x0ADA, 0x055A,
        /* 1443-1447 */ 0x0AAB, 0x0595, 0x0749, 0x0764, 0x0BAA,
        /* 1448-1452 */ 0x05B5, 0x02B6, 0x0A56, 0x0E4D, 0x0B25,
        /* 1453-1457 */ 0x0B52, 0x0B6A, 0x05AD, 0x02AE, 0x092F,
        /* 1458-1462 */ 0x0497, 0x064B, 0x06A5, 0x06AC, 0x0AD6,
        /* 1463-1467 */ 0x055D, 0x049D, 0x0A4D, 0x0D16, 0x0D95,
        /* 1468-1472 */ 0x05AA, 0x05B5, 0x02DA, 0x095B, 0x04AD,
        /* 1473-1477 */ 0x0595, 0x06CA, 0x06E4, 0x0AEA, 0x04F5,
        /* 1478-1482 */ 0x02B6, 0x0956, 0x0AAA, 0x0B54, 0x0BD2,
        /* 1483-1487 */ 0x05D9, 0x02EA, 0x096D, 0x04AD, 0x0A95,
        /* 1488-1492 */ 0x0B4A, 0x0BA5, 0x05B2, 0x09B5, 0x04D6,
        /* 1493-1497 */ 0x0A97, 0x0547, 0x0693, 0x0749, 0x0B55,
        /* 1498-1500 */ 0x056A, 0x0A6B, 0x052B,
    )

    /**
     * Epoch day (days since 1970-01-01) of 1 Muharram, one entry per year from [FIRST_YEAR].
     *
     * ICU doesn't store these: it computes them as `floor(354.36720·(year − 1300) + 460322.55)`
     * plus a per-year correction from `umAlQuraYrStartEstimateFix`, counted from the civil epoch
     * (JD 1 948 440). We ran that formula once and store the results, so there is no floating-point
     * maths at runtime. They are redundant with [MONTH_LENGTHS] (each start + that year's length =
     * the next start); the unit tests check that, which catches any copying error.
     */
    val YEAR_STARTS: IntArray = intArrayOf(
        /* 1343-1347 */ -16588, -16234, -15880, -15526, -15171,
        /* 1348-1352 */ -14816, -14462, -14107, -13753, -13399,
        /* 1353-1357 */ -13045, -12690, -12336, -11981, -11626,
        /* 1358-1362 */ -11272, -10918, -10564, -10210, -9855,
        /* 1363-1367 */ -9501, -9146, -8792, -8438, -8084,
        /* 1368-1372 */ -7729, -7375, -7020, -6665, -6311,
        /* 1373-1377 */ -5957, -5603, -5249, -4894, -4539,
        /* 1378-1382 */ -4185, -3830, -3476, -3122, -2768,
        /* 1383-1387 */ -2414, -2059, -1704, -1350, -996,
        /* 1388-1392 */ -642, -288, 67, 422, 776,
        /* 1393-1397 */ 1131, 1485, 1839, 2193, 2547,
        /* 1398-1402 */ 2902, 3256, 3611, 3965, 4319,
        /* 1403-1407 */ 4673, 5028, 5382, 5737, 6092,
        /* 1408-1412 */ 6446, 6800, 7154, 7508, 7863,
        /* 1413-1417 */ 8218, 8572, 8927, 9281, 9635,
        /* 1418-1422 */ 9989, 10344, 10698, 11053, 11407,
        /* 1423-1427 */ 11761, 12115, 12469, 12824, 13179,
        /* 1428-1432 */ 13533, 13888, 14242, 14596, 14950,
        /* 1433-1437 */ 15304, 15659, 16013, 16368, 16722,
        /* 1438-1442 */ 17076, 17430, 17785, 18139, 18494,
        /* 1443-1447 */ 18848, 19203, 19557, 19911, 20265,
        /* 1448-1452 */ 20620, 20975, 21329, 21683, 22038,
        /* 1453-1457 */ 22392, 22746, 23101, 23456, 23810,
        /* 1458-1462 */ 24165, 24519, 24873, 25227, 25581,
        /* 1463-1467 */ 25936, 26291, 26645, 26999, 27353,
        /* 1468-1472 */ 27708, 28062, 28417, 28771, 29126,
        /* 1473-1477 */ 29480, 29834, 30188, 30542, 30897,
        /* 1478-1482 */ 31252, 31606, 31960, 32314, 32668,
        /* 1483-1487 */ 33023, 33378, 33732, 34087, 34441,
        /* 1488-1492 */ 34795, 35149, 35504, 35858, 36213,
        /* 1493-1497 */ 36567, 36922, 37276, 37630, 37984,
        /* 1498-1500 */ 38339, 38693, 39048,
    )
}
