package com.example.myapplicationv2

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class menu : AppCompatActivity() {

    private lateinit var btnLanguage: ImageButton
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        // Load saved language and apply it
        val savedLanguage = sharedPreferences.getString("language", "en") ?: "en"
        applyLanguage(savedLanguage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnLanguage = findViewById(R.id.btn_language)
        btnLanguage.setOnClickListener { showLanguageMenu(it) }

        // Update button icon to match saved language
        updateLanguageIcon(getLanguageFlagResId(savedLanguage))
    }

    private fun showLanguageMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.language_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            val languageCode = when (item.itemId) {
                R.id.language_fr -> "fr"
                R.id.language_eng -> "en"
                R.id.language_esp -> "es"
                else -> "en"
            }
            changeLanguage(languageCode)
            true
        }

        popupMenu.show()
    }

    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        createConfigurationContext(config) // Create new context with updated configuration

        // Save language to SharedPreferences
        sharedPreferences.edit().putString("language", languageCode).apply()
    }

    private fun changeLanguage(languageCode: String) {
        applyLanguage(languageCode)
        // Update button icon to match selected language
        updateLanguageIcon(getLanguageFlagResId(languageCode))
    }

    private fun updateLanguageIcon(iconResId: Int) {
        btnLanguage.setImageResource(iconResId)
    }

    private fun getLanguageFlagResId(languageCode: String): Int {
        return when (languageCode) {
            "fr" -> R.drawable.fr
            "en" -> R.drawable.eng
            "es" -> R.drawable.esp
            else -> R.drawable.eng // Default to English flag
        }
    }
}
