package com.example.stoicmind

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {

    fun getLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()

        return if (!locales.isEmpty) {
            locales[0]?.language ?: "en"
        } else {
            "en"
        }
    }

    fun setLocale(language: String) {

        Log.d("LANGUAGE_TEST", "Selected language: $language")

        val localeList = LocaleListCompat.forLanguageTags(language)

        AppCompatDelegate.setApplicationLocales(localeList)

        Log.d(
            "LANGUAGE_TEST",
            "Saved application locale: ${AppCompatDelegate.getApplicationLocales().toLanguageTags()}"
        )
    }
}