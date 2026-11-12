package com.dinenest.user.data

import android.content.Context

object ThemePrefs {
    private const val FILE = "dinenest_prefs"
    private const val KEY = "dark_mode"

    fun load(context: Context, systemDefault: Boolean): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY, systemDefault)

    fun save(context: Context, dark: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY, dark)
            .apply()
    }
}