package com.oscar.orbimusic.presentation.utils

fun parseTime(duration: Long): String {
    if (duration < 0) return "0:00"
    val durationInSeconds = duration / 1000
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    return String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
}