package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class mesAffirmations :  AppCompatActivity() {  // Hérite de Base au lieu de AppCompatActivity
    private lateinit var btnNew: Button
    private lateinit var btnMyaffirmations: Button
    private lateinit var btnHowItWorks: Button
    private lateinit var btnAdvices: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mesaffirmations) // Utilisez le layout fourni
        btnNew = findViewById<Button>(R.id.btn_new)
        btnMyaffirmations = findViewById<Button>(R.id.btn_affirmation)
        btnHowItWorks = findViewById<Button>(R.id.btn_howitworks)
        btnAdvices = findViewById<Button>(R.id.btn_advicesaffirmations)


        btnNew.setOnClickListener{

            // Créez une intention pour lancer Stage2
            val intent = Intent(this, Rules::class.java)
            startActivity(intent)
        }


    }






}
