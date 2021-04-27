package com.hinnka.tsbrowser.ext

import android.content.res.Resources
import java.text.DecimalFormat

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