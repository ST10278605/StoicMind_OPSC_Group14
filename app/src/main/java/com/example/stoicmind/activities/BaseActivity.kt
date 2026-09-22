package com.example.stoicmind.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stoicmind.R

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("stoic_mind_prefs", MODE_PRIVATE)
        val selectedTheme = sharedPreferences.getString("app_theme", "classic")
        when (selectedTheme) {
            "serene" -> setTheme(R.style.Theme_StoicMind_Serene)
            "courage" -> setTheme(R.style.Theme_StoicMind_Courage)
            "nature" -> setTheme(R.style.Theme_StoicMind_Nature)
            else -> setTheme(R.style.Theme_StoicMind)
        }
        super.onCreate(savedInstanceState)
    }
}