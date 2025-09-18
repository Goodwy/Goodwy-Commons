package com.goodwy.commons.helpers

import android.text.format.DateFormat
import saman.zamani.persiandate.PersianDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

fun formatTimePart(timestamp: Long, timeFormat: String): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = timestamp
    val timeString = DateFormat.format(timeFormat, cal).toString()

    return timeString
}

fun formatShamsiDatePart(persianDate: PersianDate, pattern: String): String {
    return try {
        val localDate = LocalDate.of(
            persianDate.shYear,
            persianDate.shMonth,
            persianDate.shDay
        )
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val result = formatter.format(localDate)

        result
    } catch (_: Exception) {
        val result = formatShamsiWithPersianLocale(persianDate, pattern)

        result
    }
}

fun formatShamsiWithPersianLocale(persianDate: PersianDate, pattern: String): String {
    var result = pattern
    result = result.replace("MMMM", persianDate.monthName)
    result = result.replace("mmmm", persianDate.monthName)

    result = result.replace("yyyy", persianDate.shYear.toString())
    result = result.replace("yy", persianDate.shYear.toString().takeLast(2))
    result = result.replace("MM", persianDate.shMonth.toString().padStart(2, '0'))
    result = result.replace("M", persianDate.shMonth.toString())
    result = result.replace("dd", persianDate.shDay.toString().padStart(2, '0'))
    result = result.replace("d", persianDate.shDay.toString())

    result = result.replace("YYYY", persianDate.shYear.toString())
    result = result.replace("YY", persianDate.shYear.toString().takeLast(2))
    result = result.replace("DD", persianDate.shDay.toString().padStart(2, '0'))

    return result
}

fun isThisYearShamsi(persianDate: PersianDate): Boolean {
    val currentShamsi = PersianDate()
    return persianDate.shYear == currentShamsi.shYear
}
