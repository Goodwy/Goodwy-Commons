package com.goodwy.commons.helpers

import android.text.format.DateFormat
import saman.zamani.persiandate.PersianDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

fun formatTimePart(timestamp: Long, timeFormat: String, usePersianDigits: Boolean): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = timestamp
    val timeString = DateFormat.format(timeFormat, cal).toString()

    return if (usePersianDigits) {
        convertTimeToPersianDigits(timeString, timeFormat)
    } else {
        timeString
    }
}

fun convertTimeToPersianDigits(timeString: String, timeFormat: String): String {
    var result = timeString

    // Converting numbers
    result = convertToPersianDigits(result)

    // Replacing AM/PM with Persian equivalents
    result = result.replace("AM", "ق.ظ")
    result = result.replace("PM", "ب.ظ")
    result = result.replace("am", "ق.ظ")
    result = result.replace("pm", "ب.ظ")

    return result
}

fun formatShamsiWithPersianLocale(persianDate: PersianDate, pattern: String): String {
    var result = pattern
    result = result.replace("yyyy", convertToPersianDigits(persianDate.shYear.toString()))
    result = result.replace("yy", convertToPersianDigits(persianDate.shYear.toString().takeLast(2)))
    result = result.replace("MM", convertToPersianDigits(persianDate.shMonth.toString().padStart(2, '0')))
    result = result.replace("M", convertToPersianDigits(persianDate.shMonth.toString()))
    result = result.replace("dd", convertToPersianDigits(persianDate.shDay.toString().padStart(2, '0')))
    result = result.replace("d", convertToPersianDigits(persianDate.shDay.toString()))

    return result
}

fun convertToPersianDigits(input: String): String {
    return input.map { char ->
        when (char) {
            '0' -> '۰'
            '1' -> '۱'
            '2' -> '۲'
            '3' -> '۳'
            '4' -> '۴'
            '5' -> '۵'
            '6' -> '۶'
            '7' -> '۷'
            '8' -> '۸'
            '9' -> '۹'
            else -> char
        }
    }.joinToString("")
}

fun formatShamsiDatePart(persianDate: PersianDate, pattern: String, usePersianDigits: Boolean): String {
    return try {
        val localDate = LocalDate.of(
            persianDate.shYear,
            persianDate.shMonth,
            persianDate.shDay
        )
        val formatter = DateTimeFormatter.ofPattern(pattern)
        var result = formatter.format(localDate)

        if (usePersianDigits) {
            result = convertToPersianDigits(result)
        }
        result
    } catch (_: Exception) {
        var result = formatShamsiWithPersianLocale(persianDate, pattern)

        if (usePersianDigits) {
            result = convertToPersianDigits(result)
        }

        result
    }
}

fun isThisYearShamsi(persianDate: PersianDate): Boolean {
    val currentShamsi = PersianDate()
    return persianDate.shYear == currentShamsi.shYear
}
