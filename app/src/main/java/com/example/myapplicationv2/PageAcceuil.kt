package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import kotlinx.coroutines.*

class PageAcceuil : AppCompatActivity() {

    private val splashTimeOut: Long = 2000 // 2 secondes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_acceuil) // Utilisez le layout fourni

        // Utilisation des Coroutines pour gérer le délai
        CoroutineScope(Dispatchers.Main).launch {
            delay(splashTimeOut)
            // Démarrer l'activité suivante
            val intent = Intent(this@PageAcceuil, mesAffirmations::class.java)
            startActivity(intent)
            finish() // Finir l'activité Splash pour qu'elle ne soit pas dans le back stack
        }
    }
}