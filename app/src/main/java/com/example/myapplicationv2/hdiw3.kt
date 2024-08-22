package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class hdiw3 : Base() {  // Hérite de Base au lieu de AppCompatActivity

    override fun getLayoutId(): Int {
        return R.layout.activity_hdiw3  // Retourne le layout spécifique à cette activité
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val btnImgRulesRight = findViewById<ImageView>(R.id.btnImg_rulesright)
        val btnImgRulesLeft = findViewById<ImageView>(R.id.btnImg_left)

        // Définir un OnClickListener pour ouvrir l'activité Step2
        btnImgRulesRight.setOnClickListener {
            val intent = Intent(this, hdiw4::class.java)
            startActivity(intent)
        }
        btnImgRulesLeft.setOnClickListener {
            val intent = Intent(this, hdiw2::class.java)
            startActivity(intent)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
