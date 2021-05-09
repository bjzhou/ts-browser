package com.hinnka.tsbrowser.ext

import android.content.res.Resources
import java.text.DateFormat
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max
import kotlin.math.min

fun Long.formatByte(): String {
    val byte = this.toDouble()
    val kb = byte / 1024.0
    val mb = byte / 1024.0 / 1024.0
    val gb = byte / 1024.0 / 1024.0 / 1024.0
    val tb = byte / 1024.0 / 1024.0 / 1024.0 / 1024.0

    val format = DecimalFormat("0.00")

    return when {
        tb >= 1 -> "${format.format(tb)} TB"
        gb >= 1 -> "${format.format(gb)} GB"
        mb >= 1 -> "${format.format(mb)} MB"
        kb >= 1 -> "${format.format(kb)} KB"
        else -> "${format.format(byte)} B"
    }
}

val Int.dpx
    get() = Resources.getSystem().displayMetrics.density * this

fun Float.between(min: Float, max: Float): Float {
    if (this > max) {
        return max
    }
    if (this < min) {
        return min
    }
    return this
}

fun Long.toCalendar(): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = this@toCalendar
    }
}

fun Long.toDateString(): String {
    val dateFormat = DateFormat.getDateInstance()
    return dateFormat.format(Date(this))
}

infix fun Long.isSameDay(other: Long): Boolean {
    val calendar = toCalendar()
    val calendarOther = other.toCalendar()
    return calendar.get(Calendar.DAY_OF_YEAR) == calendarOther.get(Calendar.DAY_OF_YEAR) &&
            calendar.get(Calendar.YEAR) == calendarOther.get(Calendar.YEAR)
}