package app.afghancalendar

import java.time.LocalDate

actual fun getTodayGregorian(): GregorianDate {
    val now = LocalDate.now()
    return GregorianDate(now.year, now.monthValue, now.dayOfMonth)
}
