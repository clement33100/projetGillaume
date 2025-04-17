package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.media.MediaPlayer
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class mesAffirmationsDetails : Base() {  // Hérite de Base au lieu de AppCompatActivity

    // UI Elements
    private lateinit var textEmpty: TextView
    private lateinit var btnMainPage: Button
    private lateinit var btnNewAffirmation: Button
    private lateinit var fileContainer: LinearLayout
    private lateinit var mesaffirmationContour: LinearLayout

    override fun getLayoutId(): Int {
        return R.layout.activity_mes_affirmations_details  // Fournit le layout spécifique à cette activité
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

        // Initialisation des éléments UI
        textEmpty = findViewById(R.id.textViewmesaffirmationdetails)

        btnNewAffirmation = findViewById(R.id.btn_affirmation_new)
        btnMainPage = findViewById(R.id.btn_affirmation_menu)
        fileContainer = findViewById(R.id.fileContainer)
        mesaffirmationContour=findViewById(R.id.scrollContainermesaffirmationdetail)

        // Appeler la fonction pour afficher les fichiers
        displayAffirmationFiles(textEmpty,mesaffirmationContour)

        btnNewAffirmation.setOnClickListener {
            val intent = Intent(this, Rules::class.java)
            startActivity(intent)
        }

        btnMainPage.setOnClickListener {
            val intent = Intent(this, mesAffirmations::class.java)
            startActivity(intent)

        }


    }

    /**
     * Affiche les fichiers d'affirmations présents dans le dossier "affirmation".
     */
    private fun displayAffirmationFiles(textEdit: TextView,contour:LinearLayout) {
        // Dossier "affirmation"
        val affirmationDir = File(filesDir, "affirmation")

        // Créer le dossier s'il n'existe pas
        if (!affirmationDir.exists()) {
            affirmationDir.mkdir()
        }

        // Vérifier si le dossier contient des fichiers
        val files = affirmationDir.listFiles()
        if (files != null && files.isNotEmpty()) {
            textEdit.visibility = View.GONE
            fileContainer.removeAllViews() // Supprime les anciennes vues, si nécessaire

            // Parcourir les fichiers du dossier
            for (file in files) {
                val fileName = file.nameWithoutExtension

                // Créer un bouton pour chaque fichier
                val fileButton = Button(this).apply {
                    text = fileName
                    textSize = 24f
                    isAllCaps = false
                    setBackgroundResource(R.drawable.button_yellow_rounded) // Définir le fond personnalisé
                    setPadding(20, 20, 20, 20) // Optionnel : Ajuster le padding pour une meilleure apparence
                    setOnClickListener {
                        // Créer une Intent pour lancer l'activité ManageAffirmationActivity
                        val intent = Intent(this@mesAffirmationsDetails, ManageAffirmationActivity::class.java).apply {
                            putExtra("filePath", file.absolutePath) // Passer le chemin du fichier sélectionné
                        }
                        startActivity(intent)
                    }
                }

                // Ajouter des marges au bouton
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Largeur
                    LinearLayout.LayoutParams.WRAP_CONTENT  // Hauteur
                ).apply {
                    setMargins(40, 20, 40, 20) // Marges gauche, haut, droite, bas
                }
                fileButton.layoutParams = params

                // Ajouter le bouton au conteneur
                fileContainer.addView(fileButton)
            }
        } else {
            contour.visibility=View.GONE

            textEdit.visibility = View.VISIBLE

            // Si le dossier est vide
            Toast.makeText(this, "Aucun fichier dans le dossier affirmations.", Toast.LENGTH_SHORT).show()
        }
    }
}
