package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class rulesIntention : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rules_intention)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intention = intent.getBooleanExtra("intention", false)

        // Référencer l'ImageView par son ID
        val btnStart = findViewById<Button>(R.id.btn_start_intention)

        // Définir un OnClickListener pour ouvrir l'activité Step2
        btnStart.setOnClickListener {
            val intent = Intent(this, Etape2Voix::class.java)
            intent.putExtra("intention", intention)

            startActivity(intent)
        }


    }




}