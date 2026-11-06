package com.oscar.orbimusic.presentation.utils

fun parseTime(duration: Long): String {
    if (duration < 0) return "0:00"
    val durationInSeconds = duration / 1000
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    return String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
}

//Para la duracion total de una playlist. parseTime no sirve aqui: daria
//"187:43" para tres horas, que nadie lee. Aqui los segundos sobran.
fun parseTotalTime(duration: Long): String {
    if (duration <= 0) return "0 min"
    val totalMinutes = duration / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours == 0L -> "$totalMinutes min"
        minutes == 0L -> "$hours h"
        else -> "$hours h $minutes min"
    }
}