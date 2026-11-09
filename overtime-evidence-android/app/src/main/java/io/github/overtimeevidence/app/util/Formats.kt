package io.github.overtimeevidence.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateTime(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(millis))

fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(millis))

fun elapsedMinutes(start: Long, end: Long): Long =
    ((end - start).coerceAtLeast(0) / 60_000)

fun formatDuration(minutes: Long): String =
    "${minutes / 60}小时${minutes % 60}分钟"

fun safeFileName(original: String): String =
    original.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]+"), "_").take(100).ifBlank { "file" }
