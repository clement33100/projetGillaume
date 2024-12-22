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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rules)


        // Référencer l'ImageView par son ID
        val btnStart = findViewById<Button>(R.id.btn_start)

        // Définir un OnClickListener pour ouvrir l'activité Step2
        btnStart.setOnClickListener {
            val intent = Intent(this, step1FirstName::class.java)
            startActivity(intent)
        }


    }
}
