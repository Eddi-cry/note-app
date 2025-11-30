package com.example.noteapp.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(getSavedLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _theme = MutableStateFlow(getSavedTheme())
    val theme: StateFlow<String> = _theme.asStateFlow()

    fun setLanguage(language: String) {
        sharedPreferences.edit().putString("language", language).apply()
        _language.value = language
    }

    fun setTheme(theme: String) {
        sharedPreferences.edit().putString("theme", theme).apply()
        _theme.value = theme
    }

    private fun getSavedLanguage(): String {
        return sharedPreferences.getString("language", "ru") ?: "ru"
    }

    private fun getSavedTheme(): String {
        return sharedPreferences.getString("theme", "system") ?: "system"
    }
}