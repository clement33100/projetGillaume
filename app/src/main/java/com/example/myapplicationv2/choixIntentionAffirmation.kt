package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class choixIntentionAffirmation : AppCompatActivity() {


    private lateinit var btn_ChoixIntention: Button
    private lateinit var btn_ChoixAffirmation: Button
    var intention: Boolean=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choix_intention_affirmation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_ChoixIntention =findViewById(R.id.btn_choix_intention)
        btn_ChoixAffirmation = findViewById(R.id.btn_choix_affirmation)



        btn_ChoixIntention.setOnClickListener {

            val intent = Intent(this, rulesIntention::class.java)
            intent.putExtra("intention", intention)
            startActivity(intent)

        }

        btn_ChoixAffirmation.setOnClickListener {
            val intent = Intent(this, step1FirstName::class.java)
            startActivity(intent)

        }


    }





}