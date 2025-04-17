package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class Rules : Base() {  // Hérite de Base
    private lateinit var btnStart: Button

    override fun getLayoutId(): Int {
        return R.layout.activity_rules  // Utilisez le layout adapté
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Trouver le bouton par son ID
        btnStart = findViewById<Button>(R.id.btn_start)

        // Définir un OnClickListener pour ouvrir l'activité Etape2Voix
        btnStart.setOnClickListener {
            val intent = Intent(this, Etape2Voix::class.java)
            startActivity(intent)
        }

        // Gérer les insets pour le layout (si nécessaire)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }
}
