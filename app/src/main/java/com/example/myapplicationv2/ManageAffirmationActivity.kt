package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ManageAffirmationActivity : AppCompatActivity() {

    private lateinit var filePath: String
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_affirmation)

        // Récupérer le chemin du fichier depuis l'intent
        filePath = intent.getStringExtra("filePath") ?: run {
            Toast.makeText(this, "Chemin du fichier non trouvé.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val file = File(filePath)
        val fileNameTextView = findViewById<TextView>(R.id.fileNameTextView)
        val playButton = findViewById<Button>(R.id.playButton)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val renameButton = findViewById<Button>(R.id.renameButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Afficher le nom du fichier
        fileNameTextView.text = file.name

        // Bouton pour écouter le fichier
        playButton.setOnClickListener {
            if (file.exists()) {
                mediaPlayer?.release() // Libère tout MediaPlayer existant
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                }
                Toast.makeText(this, "Lecture de ${file.name}", Toast.LENGTH_SHORT).show()
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            } else {
                Toast.makeText(this, "Fichier introuvable.", Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton pour supprimer le fichier
        deleteButton.setOnClickListener {
            val deleted = file.delete()
            if (deleted) {
                Toast.makeText(this, "${file.name} supprimé.", Toast.LENGTH_SHORT).show()
                finish() // Retour à l'activité précédente
            } else {
                Toast.makeText(this, "Erreur lors de la suppression.", Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton pour renommer le fichier
        renameButton.setOnClickListener {
            showRenameDialog(file)
        }

        // Bouton pour retourner au menu principal
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun showRenameDialog(file: File) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rename_file, null)
        val editText = dialogView.findViewById<EditText>(R.id.renameEditText)
        editText.setText(file.nameWithoutExtension)

        val alertDialog = android.app.AlertDialog.Builder(this)
            .setTitle("Renommer le fichier")
            .setView(dialogView)
            .setPositiveButton("Renommer") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    val newFile = File(file.parent, "$newName.mp3")
                    if (file.renameTo(newFile)) {
                        Toast.makeText(this, "Fichier renommé en $newName.mp3", Toast.LENGTH_SHORT).show()
                        filePath = newFile.absolutePath // Met à jour le chemin
                        findViewById<TextView>(R.id.fileNameTextView).text = newFile.name
                    } else {
                        Toast.makeText(this, "Erreur lors du renommage.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Le nom ne peut pas être vide.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .create()

        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
