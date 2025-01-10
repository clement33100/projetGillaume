package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class mesIntentions : AppCompatActivity() {

    private lateinit var btn_intention_new: Button
    private lateinit var btn_intention_menu: Button
    var intention: Boolean=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mes_intentions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_intention_new = findViewById(R.id.btn_intention_new)
        btn_intention_menu = findViewById(R.id.btn_intention_menu)

        btn_intention_new.setOnClickListener {
            val intent = Intent(this, rulesIntention::class.java)
            intent.putExtra("intention", intention)
            startActivity(intent)
        }



    }



}