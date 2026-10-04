package com.bagadbille.tdc.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TdcDates {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.getDefault())

    fun parse(iso: String?): Instant? = iso?.let { runCatching { Instant.parse(it) }.getOrNull() }

    /** "15 Oct, 11:59 PM" in the device's time zone, or null if unparseable. */
    fun formatDateTime(iso: String?): String? =
        parse(iso)?.atZone(ZoneId.systemDefault())?.format(dateTimeFormatter)
}
