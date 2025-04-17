package com.example.myapplicationv2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class Advices : Base() {  // Hérite de Base au lieu de AppCompatActivity

    // UI Elements
    private lateinit var btnNewAdvice: Button
    private lateinit var btnMenu: Button

    override fun getLayoutId(): Int {
        return R.layout.activity_advices  // Fournit le layout spécifique à cette activité
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)
        // Configuration des Insets UI
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

        // Initialisation des éléments UI existants
        btnNewAdvice = findViewById(R.id.btnNewAdvice)
        btnMenu = findViewById(R.id.btnMenu)

        btnNewAdvice.setOnClickListener {
            val intent = Intent(this, mesAffirmationsDetails::class.java)
            startActivity(intent)
        }
        btnMenu.setOnClickListener {
            val intent = Intent(this, mesAffirmations::class.java)
            startActivity(intent)
        }
    }
}
