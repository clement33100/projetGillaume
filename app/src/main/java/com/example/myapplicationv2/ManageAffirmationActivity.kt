package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException

class ManageAffirmationActivity : Base() {  // Hérite de Base au lieu de AppCompatActivity

    private lateinit var filePath: String
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var playPauseButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private val handler = android.os.Handler()

    private lateinit var downloadButton: Button
    private lateinit var renameButton: Button
    private lateinit var deleteButton: Button
    private lateinit var backButton: Button
    private lateinit var fileNameTextView: TextView

    override fun getLayoutId(): Int {
        return R.layout.activity_manage_affirmation  // Fournit le layout spécifique à cette activité
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        fileNameTextView = findViewById(R.id.fileNameTextView)
        playPauseButton = findViewById(R.id.imageButtonPauseManageAffirmation)
        progressBar = findViewById(R.id.progressBarManageAffirmation)
        downloadButton = findViewById(R.id.downloadButton)
        renameButton = findViewById(R.id.renameButton)
        deleteButton = findViewById(R.id.deleteButton)
        backButton = findViewById(R.id.backButton)

        // Récupérer le chemin du fichier depuis l'intent
        filePath = intent.getStringExtra("filePath") ?: run {
            Toast.makeText(this, "Chemin du fichier non trouvé.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val file = File(filePath)

        // Afficher le nom du fichier
        fileNameTextView.text = file.nameWithoutExtension

        // Initialiser le MediaPlayer mais ne pas démarrer la musique
        if (file.exists()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                progressBar.max = duration / 1000 // Configurer la ProgressBar en fonction de la durée
            }
        } else {
            Toast.makeText(this, "Fichier introuvable.", Toast.LENGTH_SHORT).show()
        }

        // Bouton pour jouer ou mettre en pause le fichier
        playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        // Bouton pour télécharger le fichier
        downloadButton.setOnClickListener {
            val fileToDownload = File(filePath) // Utiliser le chemin mis à jour
            if (fileToDownload.exists()) {
                downloadFile(fileToDownload)
            } else {
                Toast.makeText(this, "Fichier introuvable pour le téléchargement.", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, mesAffirmations::class.java)
            startActivity(intent)
        }

        // Bouton pour supprimer le fichier
        deleteButton.setOnClickListener {
            val alertDialog = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_confirmation, null)
            alertDialog.setView(dialogView)

            // Créer l'AlertDialog avec un fond personnalisé
            val alert = alertDialog.create()
            alert.window?.setBackgroundDrawableResource(R.drawable.rounded_background) // Appliquer le fond arrondi
            alert.show()

            // Bouton "Confirmer"
            dialogView.findViewById<Button>(R.id.confirmButton)?.apply {
                setOnClickListener {
                    val deleted = file.delete()
                    if (deleted) {
                        Toast.makeText(this@ManageAffirmationActivity, "${file.name} supprimé.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ManageAffirmationActivity, mesAffirmationsDetails::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ManageAffirmationActivity, "Erreur lors de la suppression.", Toast.LENGTH_SHORT).show()
                    }
                    alert.dismiss()
                }
            }

            // Bouton "Annuler"
            dialogView.findViewById<Button>(R.id.cancelButton)?.apply {
                setOnClickListener {
                    alert.dismiss()
                }
            }
        }

        // Bouton pour renommer le fichier
        renameButton.setOnClickListener {
            showRenameDialog(file)
        }

    }

    private fun downloadFile(file: File) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Stockage externe non disponible.", Toast.LENGTH_SHORT).show()
            return
        }

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destFile = File(downloadsDir, file.name)

        try {
            file.copyTo(destFile, overwrite = true)
            Toast.makeText(this, "Fichier téléchargé dans ${destFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Erreur lors du téléchargement : ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ManageAffirmationActivity", "Erreur : ${e.message}")
        }
    }

    private fun playMusic() {
        mediaPlayer?.let {
            it.start()
            isPlaying = true
            playPauseButton.setImageResource(R.drawable.imgunpause) // Changer l'icône en pause
            updateProgressBar()
        }
    }

    private fun pauseMusic() {
        mediaPlayer?.let {
            it.pause()
            isPlaying = false
            playPauseButton.setImageResource(R.drawable.imgpause) // Changer l'icône en lecture
        }
    }

    private fun updateProgressBar() {
        mediaPlayer?.let {
            progressBar.progress = it.currentPosition / 1000
            if (isPlaying) {
                handler.postDelayed({ updateProgressBar() }, 1000) // Met à jour toutes les secondes
            }
        }
    }


    private fun showRenameDialog(file: File) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rename_file, null)
        val editText = dialogView.findViewById<EditText>(R.id.renameEditText)
        editText.setText(file.nameWithoutExtension)

        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        // Créez et configurez la boîte de dialogue
        val alertDialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_background)

        // Action pour le bouton "Renommer"
        confirmButton.setOnClickListener {
            val newName = editText.text.toString()
            if (newName.isNotEmpty()) {
                val newFile = File(file.parent, "$newName.mp3")
                if (file.renameTo(newFile)) {
                    Toast.makeText(this, "Fichier renommé en $newName.mp3", Toast.LENGTH_SHORT).show()
                    filePath = newFile.absolutePath // Met à jour le chemin du fichier
                    findViewById<TextView>(R.id.fileNameTextView).text = newFile.nameWithoutExtension
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(this, "Erreur lors du renommage.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Le nom ne peut pas être vide.", Toast.LENGTH_SHORT).show()
            }
        }

        // Action pour le bouton "Annuler"
        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onBackPressed() {
        val intent = Intent(this, mesAffirmationsDetails::class.java)
        startActivity(intent)
        finish() // Optionnel, pour retirer cette activité de la pile
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null) // Supprime les mises à jour restantes
    }
}
