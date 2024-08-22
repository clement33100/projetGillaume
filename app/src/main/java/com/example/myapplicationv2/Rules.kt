package com.example.myapplicationv2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Rules : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rules)

        // Référencer l'ImageView par son ID
        val btnImgRulesRight = findViewById<ImageView>(R.id.btnImg_rulesright)

        // Définir un OnClickListener pour ouvrir l'activité Step2
        btnImgRulesRight.setOnClickListener {
            val intent = Intent(this, Step2::class.java)
            startActivity(intent)
        }
        // Référencer l'ImageView par son ID
        val btnStart = findViewById<Button>(R.id.btn_start)

        // Définir un OnClickListener pour ouvrir l'activité Step2
        btnStart.setOnClickListener {
            val intent = Intent(this, Step2::class.java)
            startActivity(intent)
        }


        // Gérer les insets pour l'UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
