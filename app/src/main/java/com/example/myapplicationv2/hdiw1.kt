package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class hdiw1 : Base() {

    override fun getLayoutId(): Int {
        return R.layout.activity_hdiw1  // Retourne le layout spécifique à cette activité
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)
        // Trouver les boutons par leur ID
        val btnNew = findViewById<Button>(R.id.btnNewAdvice)
        val btnMenu = findViewById<Button>(R.id.btnMenu)

        // Définir les OnClickListener
        btnNew.setOnClickListener {
            val intent = Intent(this, Rules::class.java)
            startActivity(intent)
        }

        btnMenu.setOnClickListener {
            val intent = Intent(this, mesAffirmations::class.java)
            startActivity(intent)
        }

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
