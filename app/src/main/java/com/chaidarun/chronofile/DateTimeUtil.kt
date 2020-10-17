package com.chaidarun.chronofile

import java.text.SimpleDateFormat
import java.util.*

/** Gets current Unix timestamp in seconds */
fun epochSeconds() = System.currentTimeMillis() / 1000

private val dateFormat by lazy { SimpleDateFormat("EE, d MMM yyyy", Locale.US) }
private val timeFormat by lazy { SimpleDateFormat("H:mm", Locale.US) }

fun formatDate(date: Date): String = dateFormat.format(date)
fun formatDate(seconds: Long) = formatDate(Date(seconds * 1000))
fun formatTime(date: Date): String = timeFormat.format(date)

/** Pretty-prints time given in seconds, e.g. 86461 -> "1d 1m" */
fun formatDuration(seconds: Long): String {
  if (seconds < 30) return "0m"

  // Rounds to nearest minute
  val adjustedSeconds = if (seconds % 60 < 30) seconds else seconds + 60

  val pieces = mutableListOf<String>()
  val totalMinutes = adjustedSeconds / 60
  val minutes = totalMinutes % 60
  if (minutes != 0L) {
    pieces.add(0, "${minutes}m")
  }
  val totalHours = totalMinutes / 60
  val hours = totalHours % 24
  if (hours != 0L) {
    pieces.add(0, "${hours}h")
  }
  val days = totalHours / 24
  if (days != 0L) {
    pieces.add(0, "${days}d")
  }
  return pieces.joinToString(" ")
}

/** Gets the timestamp of the last midnight that occurred before the given timestamp */
fun getPreviousMidnight(timestamp: Long) =
  with(Calendar.getInstance().apply { timeInMillis = timestamp * 1000 }) {
    GregorianCalendar(
      get(Calendar.YEAR),
      get(Calendar.MONTH),
      get(Calendar.DAY_OF_MONTH)
    ).time.time / 1000
  }
