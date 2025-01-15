package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class mesAffirmationsDetails : AppCompatActivity() {

    private lateinit var textEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mes_affirmations_details)

        // Gestion des insets pour adapter la vue à l'écran
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        textEmpty=findViewById(R.id.textViewmesaffirmationdetails)

        // Appeler la fonction pour afficher les fichiers
        displayAffirmationFiles(textEmpty)


    }

    private fun displayAffirmationFiles(textEdit:TextView) {



        // Dossier "affirmation"
        val affirmationDir = File(filesDir, "affirmation")

        // Créer le dossier s'il n'existe pas
        if (!affirmationDir.exists()) {
            affirmationDir.mkdir()
        }

        // Vérifier si le dossier contient des fichiers
        val files = affirmationDir.listFiles()
        if (files != null && files.isNotEmpty()) {
            val container = findViewById<LinearLayout>(R.id.fileContainer) // Assurez-vous que le layout existe
            container.removeAllViews() // Supprime les anciennes vues, si nécessaire

            // Parcourir les fichiers du dossier
            for (file in files) {
                textEdit.visibility=View.GONE

                val fileName = file.name

                // Créer un bouton pour chaque fichier
                val fileButton = Button(this).apply {
                    text = fileName
                    setOnClickListener {
                        // Créer une Intent pour lancer l'activité ManageAffirmationActivity
                        val intent = Intent(this@mesAffirmationsDetails, ManageAffirmationActivity::class.java).apply {
                            putExtra("filePath", file.absolutePath) // Passer le chemin du fichier sélectionné
                        }
                        startActivity(intent)
                    }
                }

                // Ajouter le bouton au conteneur
                container.addView(fileButton)
            }
        } else {
            textEdit.visibility=View.VISIBLE

            // Si le dossier est vide
            Toast.makeText(this, "Aucun fichier dans le dossier affirmations.", Toast.LENGTH_SHORT).show()
        }
    }
}
