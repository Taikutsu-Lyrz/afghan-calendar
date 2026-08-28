package app.afghancalendar

data class GregorianDate(val year: Int, val month: Int, val day: Int)
data class JalaliDate(val year: Int, val month: Int, val day: Int)
data class HijriDate(val year: Int, val month: Int, val day: Int)

private val persianDigits = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
fun toPersianDigits(input: String): String {
    val sb = StringBuilder()
    for (c in input) {
        if (c in '0'..'9') sb.append(persianDigits[c - '0']) else sb.append(c)
    }
    return sb.toString()
}
fun toPersianDigits(num: Int): String = toPersianDigits(num.toString())
fun toPersianDigitsPadded(num: Int, len: Int): String = toPersianDigits(num.toString().padStart(len,'0'))

val jalaliMonthNames = arrayOf(
    "", "حمل","ثور","جوزا","سرطان","اسد","سنبله","میزان","عقرب","قوس","جدی","دلو","حوت"
)
val hijriMonthNames = arrayOf(
    "", "محرم","صفر","ربیع‌الاول","ربیع‌الثانی","جمادی‌الاول","جمادی‌الثانی","رجب","شعبان","رمضان","شوال","ذوالقعدة","ذوالحجة"
)
val hijriMonthNamesShort = hijriMonthNames
val gregorianMonthNamesEn = arrayOf("", "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

// Persian weekdays Sunday-start, rightmost Sunday as per screenshot
val persianWeekdays = arrayOf("یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه","شنبه")
// Large centered weekday names for bottom panel same array
val persianWeekdaysLarge = persianWeekdays

// For grid header: screenshot order from right to left: یکشنبه, دوشنبه, سه‌شنبه, چهارشنبه, پنجشنبه, جمعه, شنبه
// Visually RTL, but logically we store Sunday=0...Saturday=6. When rendering RTL, column 0 rightmost = Sunday.
val weekdayHeaderSundayStart = arrayOf("یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه","شنبه")

// Conversion algorithms

fun gregorianToJdn(y: Int, m: Int, d: Int): Int {
    val a = (14 - m) / 12
    val yy = y + 4800 - a
    val mm = m + 12 * a - 3
    return d + (153 * mm + 2) / 5 + 365 * yy + yy / 4 - yy / 100 + yy / 400 - 32045
}

fun jdnToGregorian(jdn: Int): GregorianDate {
    val a = jdn + 32044
    val b = (4 * a + 3) / 146097
    val c = a - (146097 * b) / 4
    val d = (4 * c + 3) / 1461
    val e = c - (1461 * d) / 4
    val mm = (5 * e + 2) / 153
    val day = e - (153 * mm + 2) / 5 + 1
    val month = mm + 3 - 12 * (mm / 10)
    val year = b * 100 + d - 4800 + (mm / 10)
    return GregorianDate(year, month, day)
}

// Jalali 2820-year cycle (Birashk/Ahmad Birashk) - matches jalaali-js for 1405/6/6
fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Int {
    val epbase = jy - if (jy >= 0) 474 else 473
    val epyear = 474 + (epbase % 2820)
    val mdays = if (jm <= 7) (jm - 1) * 31 else (jm - 1) * 30 + 6
    return jd + mdays + (epyear * 682 - 110) / 2816 + (epyear - 1) * 365 + (epbase / 2820) * 1029983 + 1948320
}

fun jdnToJalali(jdn: Int): JalaliDate {
    val depoch = jdn - jalaliToJdn(475, 1, 1)
    val cycle = depoch / 1029983
    val cyear = depoch % 1029983
    val ycycle: Int
    if (cyear == 1029982) {
        ycycle = 2820
    } else {
        val aux1 = cyear / 366
        val aux2 = cyear % 366
        ycycle = (2134 * aux1 + 2816 * aux2 + 2815) / 1028522 + aux1 + 1
    }
    var jy = ycycle + 2820 * cycle + 474
    if (jy <= 0) jy--
    val jdn1f = jalaliToJdn(jy, 1, 1)
    val k = jdn - jdn1f
    val jm: Int
    val jd: Int
    if (k <= 185) {
        jm = 1 + k / 31
        jd = 1 + k % 31
    } else {
        val kk = k - 186
        jm = 7 + kk / 30
        jd = 1 + kk % 30
    }
    return JalaliDate(jy, jm, jd)
}

fun gregorianToJalali(g: GregorianDate): JalaliDate {
    return jdnToJalali(gregorianToJdn(g.year, g.month, g.day))
}
fun jalaliToGregorian(j: JalaliDate): GregorianDate {
    return jdnToGregorian(jalaliToJdn(j.year, j.month, j.day))
}

// Hijri tabular Islamic civil calendar
// We add +1 offset to match requirement: 2026-08-28 => 1448/03/16
private const val HIJRI_OFFSET = 1

fun hijriToJdn(y: Int, m: Int, d: Int): Int {
    return d + kotlin.math.ceil(29.5 * (m - 1)).toInt() + (y - 1) * 354 + (3 + 11 * y) / 30 + 1948439 - 1
}

fun jdnToHijri(jdn: Int): HijriDate {
    // Apply offset: we store hijri such that greg JDN + OFFSET maps to correct hijri
    val adjJdn = jdn + HIJRI_OFFSET
    // Approximate year
    var y = (30 * (adjJdn - 1948440) + 10632) / 10631
    if (y < 1) y = 1
    // Correct y
    while (hijriToJdn(y + 1, 1, 1) <= adjJdn) y++
    while (hijriToJdn(y, 1, 1) > adjJdn) y--
    var m = 1
    while (m < 12 && hijriToJdn(y, m + 1, 1) <= adjJdn) m++
    val d = adjJdn - hijriToJdn(y, m, 1) + 1
    return HijriDate(y, m, d)
}

fun gregorianToHijri(g: GregorianDate): HijriDate {
    return jdnToHijri(gregorianToJdn(g.year, g.month, g.day))
}

fun weekdayFromJdn(jdn: Int): Int {
    // Sunday=0 ... Saturday=6 ; (jdn+1)%7 gives Sunday 0
    return (jdn + 1) % 7
}
fun weekdayFromGregorian(g: GregorianDate): Int = weekdayFromJdn(gregorianToJdn(g.year, g.month, g.day))
fun weekdayFromJalali(j: JalaliDate): Int = weekdayFromJdn(jalaliToJdn(j.year, j.month, j.day))

fun jalaliMonthLength(jy: Int, jm: Int): Int {
    if (jm <= 6) return 31
    if (jm <= 11) return 30
    // Esfand: 29 or 30 depending on leap
    val jdn1 = jalaliToJdn(jy, 12, 1)
    val jdnNext = jalaliToJdn(jy + 1, 1, 1)
    return jdnNext - jdn1
}

fun isJalaliLeap(jy: Int): Boolean {
    return jalaliMonthLength(jy, 12) == 30
}
