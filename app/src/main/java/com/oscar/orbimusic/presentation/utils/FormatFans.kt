package com.oscar.orbimusic.presentation.utils

import java.util.Locale

fun formatFans(fans: Int): String = when {
    fans >= 1_000_000 -> {
        val millions = fans / 1_000_000.0
        //Un decimal solo si aporta: "12 M" y no "12.0 M"
        val text = if (millions >= 10) millions.toInt().toString()
        else String.format(Locale.US, "%.1f", millions).removeSuffix(".0")
        "$text M fans"
    }
    fans >= 1_000 -> "${fans / 1_000} mil fans"
    fans == 1 -> "1 fan"
    else -> "$fans fans"
}
