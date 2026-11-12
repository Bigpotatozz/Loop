package com.oscar.orbimusic.data.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _darkTheme = MutableStateFlow(readStored())
    val darkTheme: StateFlow<Boolean?> = _darkTheme.asStateFlow()

    fun setDarkTheme(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply()
        _darkTheme.value = dark
    }

    private fun readStored(): Boolean? =
        if (prefs.contains(KEY_DARK_THEME)) prefs.getBoolean(KEY_DARK_THEME, false) else null

    private companion object {
        const val PREFS_NAME = "settings"
        const val KEY_DARK_THEME = "dark_theme"
    }
}
