package com.example.myapplicationv2

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

import kotlinx.coroutines.*

import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.Locale

class PageAcceuil : AppCompatActivity() {

    private val splashTimeOut: Long = 2000 // 2 secondes
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_acceuil) // Utilisez le layout fourni
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)




        // Utilisation des Coroutines pour gérer le délai
        CoroutineScope(Dispatchers.Main).launch {
            delay(splashTimeOut)
            // Démarrer l'activité suivante
            val intent = Intent(this@PageAcceuil, mesAffirmations::class.java)
            startActivity(intent)
            finish() // Finir l'activité Splash pour qu'elle ne soit pas dans le back stack
        }

    }

    override fun attachBaseContext(newBase: Context) {
        val locale = Locale("fr")
        val config = Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}