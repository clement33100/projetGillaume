package com.example.myapplicationv2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.util.Locale

abstract class Base : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var btnLanguage: ImageButton
    private lateinit var btnBurger: ImageButton
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        // Initialiser le DrawerLayout et le NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleMenuItemClick(menuItem)
            drawerLayout.closeDrawers()
            true
        }

        // Configurer le bouton du menu burger pour ouvrir le drawer
        btnBurger = findViewById(R.id.btn_burger)
        btnBurger.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Initialiser les préférences partagées pour la langue
        sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        // Charger la langue sauvegardée et l'appliquer
        val savedLanguage = sharedPreferences.getString("language", "en") ?: "en"
        applyLanguage(savedLanguage)

        // Configurer le bouton de sélection de langue
        btnLanguage = findViewById(R.id.btn_language)
        btnLanguage.setOnClickListener { showLanguageMenu(it) }

        // Mettre à jour l'icône du bouton en fonction de la langue sauvegardée
        updateLanguageIcon(getLanguageFlagResId(savedLanguage))

        // Gérer les insets pour le layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    abstract fun getLayoutId(): Int

    private fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.btn_menu -> {
                val intent = Intent(this, ChoseBelieve::class.java)
                startActivity(intent)
            }
            R.id.btn_new -> {
                val intent = Intent(this, Rules::class.java)
                startActivity(intent)
            }
            R.id.btn_advices -> {
                val intent = Intent(this, Advices::class.java)
                startActivity(intent)
            }
            R.id.btn_visu -> {
                val intent = Intent(this, Visualize::class.java)
                startActivity(intent)
            }
            R.id.btn_hdiw -> {
                val intent = Intent(this, hdiw1::class.java)
                startActivity(intent)
            }
            R.id.btn_affirm -> {
                val intent = Intent(this, mesAffirmations::class.java)
                startActivity(intent)
            }


            // Autres actions pour les autres boutons du menu
        }
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
        createConfigurationContext(config)

        // Enregistrer la langue dans SharedPreferences
        sharedPreferences.edit().putString("language", languageCode).apply()
    }

    private fun changeLanguage(languageCode: String) {
        applyLanguage(languageCode)
        // Mettre à jour l'icône du bouton en fonction de la langue sélectionnée
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
            else -> R.drawable.eng // Par défaut, icône pour l'anglais
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
