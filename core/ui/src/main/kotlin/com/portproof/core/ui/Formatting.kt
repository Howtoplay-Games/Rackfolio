package com.portproof.core.ui

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Locale-aware date formatting (e.g. "12 Mar 2026" or "Mar 12, 2026"). */
object PortProofFormats {
    fun date(date: LocalDate, locale: Locale): String =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date)

    fun dayOfWeekShort(date: LocalDate, locale: Locale): String =
        DateTimeFormatter.ofPattern("EEE", locale).format(date)

    fun dayAndMonth(date: LocalDate, locale: Locale): String =
        DateTimeFormatter.ofPattern("d MMM", locale).format(date)
}

@Composable
@ReadOnlyComposable
fun currentLocale(): Locale = LocalConfiguration.current.locales.get(0) ?: Locale.ROOT

@Composable
@ReadOnlyComposable
fun formatDate(date: LocalDate): String = PortProofFormats.date(date, currentLocale())

/** "5 min. ago", "Yesterday", ... via the platform formatter so it is localised. */
@Composable
fun relativeTime(instant: Instant, now: Long = System.currentTimeMillis()): String {
    val millis = instant.toEpochMilli()
    if (now - millis in 0 until DateUtils.MINUTE_IN_MILLIS) return stringResource(R.string.ui_time_just_now)
    return DateUtils.getRelativeTimeSpanString(millis, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
        .toString()
}
