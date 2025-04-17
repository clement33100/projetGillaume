package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class mesAffirmations : Base() {  // Hérite de Base au lieu de AppCompatActivity

    private lateinit var btnNew: Button
    private lateinit var btnMyaffirmations: Button
    private lateinit var btnHowItWorks: Button
    private lateinit var btnAdvices: Button
    private lateinit var btnAffirmationsDetails: Button

    override fun getLayoutId(): Int {
        return R.layout.activity_mesaffirmations  // Utilisez le layout adapté
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Configuration des vues
        btnNew = findViewById<Button>(R.id.btn_new)
        btnMyaffirmations = findViewById<Button>(R.id.btn_affirmation)
        btnHowItWorks = findViewById<Button>(R.id.btn_howitworks)
        btnAdvices = findViewById<Button>(R.id.btn_advicesaffirmations)
        btnAffirmationsDetails = findViewById<Button>(R.id.btn_affirmation)  // Assurez-vous que l'ID est correct

        // Définir les OnClickListeners
        btnNew.setOnClickListener {
            // Créez une intention pour lancer choixIntentionAffirmation
            val intent = Intent(this, Rules::class.java)
            startActivity(intent)
        }

        btnHowItWorks.setOnClickListener {
            val intent = Intent(this, hdiw1::class.java)
            startActivity(intent)
        }

        btnAdvices.setOnClickListener {
            val intent = Intent(this, Advices::class.java)
            startActivity(intent)
        }

        btnAffirmationsDetails.setOnClickListener {
            val intent = Intent(this, mesAffirmationsDetails::class.java)
            startActivity(intent)
        }

        // Gestion des insets pour le layout (si nécessaire)
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
