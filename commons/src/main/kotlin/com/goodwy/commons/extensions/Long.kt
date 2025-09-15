package com.goodwy.commons.extensions

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.text.format.Time
import saman.zamani.persiandate.PersianDate
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

fun Long.formatSize(): String {
    if (this <= 0) {
        return "0 B"
    }

    val units = arrayOf("B", "kB", "MB", "GB", "TB", "PB", "EB")
    val digitGroups = (Math.log10(toDouble()) / Math.log10(1024.0)).toInt()
    return "${DecimalFormat("#,##0.#").format(this / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
}

//fun Long.formatDate(context: Context, dateFormat: String? = null, timeFormat: String? = null): String {
//    val useDateFormat = dateFormat ?: context.baseConfig.dateFormat
//    val useTimeFormat = timeFormat ?: context.getTimeFormat()
//    val cal = Calendar.getInstance(Locale.ENGLISH)
//    cal.timeInMillis = this
//    return DateFormat.format("$useDateFormat, $useTimeFormat", cal).toString()
//}

fun Long.formatDate(
    context: Context,
    dateFormat: String? = null,
    timeFormat: String? = null,
    useShamsi: Boolean? = null,
    usePersianDigits: Boolean? = null
): String {
    val useDateFormat = dateFormat ?: context.baseConfig.dateFormat
    val useTimeFormat = timeFormat ?: context.getTimeFormat()
    val isUseShamsi = useShamsi ?: context.baseConfig.useShamsi
    val isUsePersianDigits = usePersianDigits ?: context.baseConfig.usePersianDigits

    return if (isUseShamsi) {
        formatWithShamsiAdvanced(useDateFormat, useTimeFormat, isUsePersianDigits)
    } else {
        formatWithGregorian(useDateFormat, useTimeFormat)
    }
}

private fun Long.formatWithGregorian(dateFormat: String, timeFormat: String): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    val datePart = DateFormat.format(dateFormat, cal).toString()
    val timePart = DateFormat.format(timeFormat, cal).toString()
    return "$datePart, $timePart"
}

private fun Long.formatWithShamsiAdvanced(dateFormat: String, timeFormat: String, usePersianDigits: Boolean): String {
    val persianDate = PersianDate(this)

    val shamsiDatePart = formatShamsiDatePart(persianDate, dateFormat, usePersianDigits)
    val timePart = formatTimePart(this, timeFormat, usePersianDigits)

    return "$shamsiDatePart, $timePart"
}

private fun formatTimePart(timestamp: Long, timeFormat: String, usePersianDigits: Boolean): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = timestamp
    val timeString = DateFormat.format(timeFormat, cal).toString()

    return if (usePersianDigits) {
        convertTimeToPersianDigits(timeString, timeFormat)
    } else {
        timeString
    }
}

private fun convertTimeToPersianDigits(timeString: String, timeFormat: String): String {
    var result = timeString

    // Конвертируем цифры
    result = convertToPersianDigits(result)

    // Заменяем AM/PM на персидские эквиваленты
    result = result.replace("AM", "ق.ظ")
    result = result.replace("PM", "ب.ظ")
    result = result.replace("am", "ق.ظ")
    result = result.replace("pm", "ب.ظ")

    return result
}

private fun formatShamsiWithPersianLocale(persianDate: PersianDate, pattern: String): String {
    var result = pattern
    result = result.replace("yyyy", convertToPersianDigits(persianDate.shYear.toString()))
    result = result.replace("yy", convertToPersianDigits(persianDate.shYear.toString().takeLast(2)))
    result = result.replace("MM", convertToPersianDigits(persianDate.shMonth.toString().padStart(2, '0')))
    result = result.replace("M", convertToPersianDigits(persianDate.shMonth.toString()))
    result = result.replace("dd", convertToPersianDigits(persianDate.shDay.toString().padStart(2, '0')))
    result = result.replace("d", convertToPersianDigits(persianDate.shDay.toString()))

    return result
}

private fun convertToPersianDigits(input: String): String {
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

//fun Long.formatTime(context: Context): String {
//    val cal = Calendar.getInstance(Locale.ENGLISH)
//    cal.timeInMillis = this
//    return DateFormat.format(context.getTimeFormat(), cal).toString()
//}

fun Long.formatTime(context: Context, useShamsi: Boolean? = null, usePersianDigits: Boolean? = null): String {
    val isUseShamsi = useShamsi ?: context.baseConfig.useShamsi
    val isUsePersianDigits = usePersianDigits ?: context.baseConfig.usePersianDigits
    return if (isUseShamsi) {
        formatShamsiTime(context, isUsePersianDigits)
    } else {
        formatGregorianTime(context)
    }
}

private fun Long.formatGregorianTime(context: Context): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    return DateFormat.format(context.getTimeFormat(), cal).toString()
}

private fun Long.formatShamsiTime(context: Context, usePersianDigits: Boolean): String {
    val timeFormat = context.getTimeFormat()
    val timeString = formatGregorianTime(context)

    return if (usePersianDigits) {
        convertTimeToPersianDigits(timeString, timeFormat)
    } else {
        timeString
    }
}

//fun Long.formatDateOrTime(
//    context: Context,
//    hideTimeOnOtherDays: Boolean,
//    showCurrentYear: Boolean,
//    hideTodaysDate: Boolean = true,
//): String {
//    val cal = Calendar.getInstance(Locale.ENGLISH)
//    cal.timeInMillis = this
//
//    return if (hideTodaysDate && DateUtils.isToday(this)) {
//        DateFormat.format(context.getTimeFormat(), cal).toString()
//    } else {
//        var format = context.baseConfig.dateFormat
//        if (!showCurrentYear && isThisYear()) {
//            format = format.replace("y", "").trim().trim('-').trim('.').trim('/')
//        }
//
//        if (!hideTimeOnOtherDays) {
//            format += ", ${context.getTimeFormat()}"
//        }
//
//        DateFormat.format(format, cal).toString()
//    }
//}

fun Long.formatDateOrTime(
    context: Context,
    hideTimeOnOtherDays: Boolean,
    showCurrentYear: Boolean,
    hideTodaysDate: Boolean = true,
    useShamsi: Boolean? = null,
    usePersianDigits: Boolean? = null
): String {
    val isUseShamsi = useShamsi ?: context.baseConfig.useShamsi
    val isUsePersianDigits = usePersianDigits ?: context.baseConfig.usePersianDigits
    return if (isUseShamsi) {
        formatDateOrTimeShamsi(
            context,
            hideTimeOnOtherDays,
            showCurrentYear,
            hideTodaysDate,
            isUsePersianDigits
        )
    } else {
        formatDateOrTimeGregorian(
            context,
            hideTimeOnOtherDays,
            showCurrentYear,
            hideTodaysDate
        )
    }
}

private fun Long.formatDateOrTimeGregorian(
    context: Context,
    hideTimeOnOtherDays: Boolean,
    showCurrentYear: Boolean,
    hideTodaysDate: Boolean
): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this

    return if (hideTodaysDate && DateUtils.isToday(this)) {
        DateFormat.format(context.getTimeFormat(), cal).toString()
    } else {
        var format = context.baseConfig.dateFormat
        if (!showCurrentYear && isThisYear()) {
            format = format.replace("y", "").trim().trim('-').trim('.').trim('/')
        }

        if (!hideTimeOnOtherDays) {
            format += ", ${context.getTimeFormat()}"
        }

        DateFormat.format(format, cal).toString()
    }
}

private fun Long.formatDateOrTimeShamsi(
    context: Context,
    hideTimeOnOtherDays: Boolean,
    showCurrentYear: Boolean,
    hideTodaysDate: Boolean,
    usePersianDigits: Boolean
): String {
    val persianDate = PersianDate(this)

    return if (hideTodaysDate && DateUtils.isToday(this)) {
        // Only time for today
        formatShamsiTime(context, usePersianDigits)
    } else {
        // Date or date + time for other days
        var dateFormat = context.baseConfig.dateFormat

        // Remove the year if it is not necessary to display it
        if (!showCurrentYear && isThisYearShamsi(persianDate)) {
            dateFormat = dateFormat.replace("y", "").trim().trim('-').trim('.').trim('/')
        }

        // Formatting the date of Shamsi
        val datePart = formatShamsiDatePart(persianDate, dateFormat, usePersianDigits)

        if (!hideTimeOnOtherDays) {
            // Add time
            val timePart = formatShamsiTime(context, usePersianDigits)
            return if (usePersianDigits) {
                "$datePart، $timePart"
            } else {
                "$datePart, $timePart"
            }
        } else {
            return datePart
        }
    }
}

private fun formatShamsiDatePart(persianDate: PersianDate, pattern: String, usePersianDigits: Boolean): String {
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

private fun isThisYearShamsi(persianDate: PersianDate): Boolean {
    val currentShamsi = PersianDate()
    return persianDate.shYear == currentShamsi.shYear
}

fun Long.isThisYear(): Boolean {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    val currentCal = Calendar.getInstance()
    return cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)
}

//fun Long.toDayCode(format: String = "ddMMyy"): String {
//    val cal = Calendar.getInstance(Locale.ENGLISH)
//    cal.timeInMillis = this
//    return DateFormat.format(format, cal).toString()
//}

fun Long.toDayCode(
    context: Context,
    format: String = "ddMMyy",
    useShamsi: Boolean? = null,
    usePersianDigits: Boolean? = null
): String {
    val isUseShamsi = useShamsi ?: context.baseConfig.useShamsi
    val isUsePersianDigits = usePersianDigits ?: context.baseConfig.usePersianDigits
    return if (isUseShamsi) {
        toShamsiDayCode(format, isUsePersianDigits)
    } else {
        toGregorianDayCode(format)
    }
}

private fun Long.toGregorianDayCode(format: String): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    return DateFormat.format(format, cal).toString()
}

private fun Long.toShamsiDayCode(format: String, usePersianDigits: Boolean): String {
    val persianDate = PersianDate(this)

    var result = format

    result = result.replace("yyyy", persianDate.shYear.toString())
    result = result.replace("yy", persianDate.shYear.toString().takeLast(2))
    result = result.replace("MM", persianDate.shMonth.toString().padStart(2, '0'))
    result = result.replace("M", persianDate.shMonth.toString())
    result = result.replace("dd", persianDate.shDay.toString().padStart(2, '0'))
    result = result.replace("d", persianDate.shDay.toString())

    result = result.replace("YYYY", persianDate.shYear.toString())
    result = result.replace("YY", persianDate.shYear.toString().takeLast(2))
    result = result.replace("DD", persianDate.shDay.toString().padStart(2, '0'))

    return if (usePersianDigits) {
        convertToPersianDigits(result)
    } else {
        result
    }
}
