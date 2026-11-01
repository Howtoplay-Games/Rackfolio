package com.portproof.core.ui

import androidx.annotation.StringRes
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@StringRes
fun greetingRes(time: LocalTime = LocalTime.now()): Int = when (time.hour) {
    in 5..11 -> R.string.ui_greeting_morning
    in 12..17 -> R.string.ui_greeting_afternoon
    else -> R.string.ui_greeting_evening
}

/** "Sunday, 1 Nov" style date line shown above the greeting (localised order and names). */
fun shortDateLine(date: LocalDate, locale: Locale): String =
    DateTimeFormatter.ofPattern(android.text.format.DateFormat.getBestDateTimePattern(locale, "EEEEdMMM"), locale).format(date)
