package com.goodwy.commons.extensions

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.goodwy.commons.helpers.*
import saman.zamani.persiandate.PersianDate
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

fun Long.formatSize(): String {
    if (this <= 0) {
        return "0 B"
    }

    val units = arrayOf("B", "kB", "MB", "GB", "TB", "PB", "EB")
    val digitGroups = (log10(toDouble()) / log10(1024.0)).toInt()
    return "${DecimalFormat("#,##0.#").format(this / 1024.0.pow(digitGroups.toDouble()))} ${units[digitGroups]}"
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
    useRelativeDate: Boolean = false, //Restriction: always uses the system time format (12 or 24)
    transitionResolution: Long = 2.days.inWholeMilliseconds
): String {
    val useDateFormat = dateFormat ?: context.baseConfig.dateFormat
    val useTimeFormat = timeFormat ?: context.getTimeFormat()
    val isUseShamsi = useShamsi ?: context.baseConfig.useShamsi

    val isUseRelativeDate = useRelativeDate && (System.currentTimeMillis() - this <= transitionResolution)
    return if (isUseRelativeDate) {
        DateUtils.getRelativeDateTimeString(
            context,
            this,
            1.minutes.inWholeMilliseconds,
            transitionResolution,
            0
        ).toString()
    } else {
        if (isUseShamsi) {
            formatWithShamsiAdvanced(useDateFormat, useTimeFormat)
        } else {
            formatWithGregorian(useDateFormat, useTimeFormat)
        }
    }
}

private fun Long.formatWithGregorian(dateFormat: String, timeFormat: String): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    val datePart = DateFormat.format(dateFormat, cal).toString()
    val timePart = DateFormat.format(timeFormat, cal).toString()
    return "$datePart, $timePart"
}

private fun Long.formatWithShamsiAdvanced(dateFormat: String, timeFormat: String): String {
    val persianDate = PersianDate(this)

    val shamsiDatePart = formatShamsiDatePart(persianDate, dateFormat)
    val timePart = formatTimePart(this, timeFormat)

    return "$shamsiDatePart, $timePart"
}

//fun Long.formatTime(context: Context): String {
//    val cal = Calendar.getInstance(Locale.ENGLISH)
//    cal.timeInMillis = this
//    return DateFormat.format(context.getTimeFormat(), cal).toString()
//}

fun Long.formatTime(
    context: Context,
    timeFormat: String? = null
): String {
    val mTimeFormat = timeFormat ?: context.getTimeFormat()
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    return DateFormat.format(mTimeFormat, cal).toString()
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
    dateFormat: String? = null,
    timeFormat: String? = null,
    useShamsi: Boolean? = null
): String {
    val useDateFormat = dateFormat ?: context.baseConfig.dateFormat
    val useTimeFormat = timeFormat ?: context.getTimeFormat()
    val isUseShamsi = useShamsi ?: context.baseConfig.useShamsi

    return if (isUseShamsi) {
        formatDateOrTimeShamsi(
            context,
            hideTimeOnOtherDays,
            showCurrentYear,
            hideTodaysDate,
            useDateFormat,
            useTimeFormat
        )
    } else {
        formatDateOrTimeGregorian(
            context,
            hideTimeOnOtherDays,
            showCurrentYear,
            hideTodaysDate,
            useDateFormat,
            useTimeFormat
        )
    }
}

private fun Long.formatDateOrTimeGregorian(
    context: Context,
    hideTimeOnOtherDays: Boolean,
    showCurrentYear: Boolean,
    hideTodaysDate: Boolean,
    dateFormat: String,
    timeFormat: String
): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this

    return if (hideTodaysDate && DateUtils.isToday(this)) {
        DateFormat.format(timeFormat, cal).toString()
    } else {
        var format = dateFormat
        if (!showCurrentYear && isThisYear()) {
            format = format.replace("y", "").trim().trim('-').trim('.').trim('/')
        }

        if (!hideTimeOnOtherDays) {
            format += ", ${timeFormat}"
        }

        DateFormat.format(format, cal).toString()
    }
}

private fun Long.formatDateOrTimeShamsi(
    context: Context,
    hideTimeOnOtherDays: Boolean,
    showCurrentYear: Boolean,
    hideTodaysDate: Boolean,
    dateFormat: String,
    timeFormat: String
): String {
    val persianDate = PersianDate(this)

    return if (hideTodaysDate && DateUtils.isToday(this)) {
        // Only time for today
        formatTime(context, timeFormat)
    } else {
        // Date or date + time for other days
        var mDateFormat = dateFormat

        // Remove the year if it is not necessary to display it
        if (!showCurrentYear && isThisYearShamsi(persianDate)) {
            mDateFormat = mDateFormat.replace("y", "").trim().trim('-').trim('.').trim('/')
        }

        // Formatting the date of Shamsi
        val datePart = formatShamsiDatePart(persianDate, mDateFormat)

        if (!hideTimeOnOtherDays) {
            // Add time
            val timePart = formatTime(context, timeFormat)
            return "$datePart, $timePart"
        } else {
            return datePart
        }
    }
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
    useShamsi: Boolean? = null
): String {
    val isUseShamsi = useShamsi ?: context.baseConfig.useShamsi
    return if (isUseShamsi) {
        toDayCodeShamsi(format)
    } else {
        toDayCodeGregorian(format)
    }
}

fun Long.toDayCodeGregorian(format: String = "ddMMyy"): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    return DateFormat.format(format, cal).toString()
}

fun Long.toDayCodeShamsi(format: String = "ddMMyy"): String {
    val persianDate = PersianDate(this)
    val datePart = formatShamsiDatePart(persianDate, format)

    return datePart
}
