package com.example.myapplicationv2

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File
import java.io.IOException

class ManageAffirmationActivity : Base() {

    private lateinit var filePath: String

    // 1. Champ ExoPlayer
    private var exoPlayer: ExoPlayer? = null

    private lateinit var downloadButton: Button
    private lateinit var renameButton: Button
    private lateinit var deleteButton: Button
    private lateinit var backButton: Button
    private lateinit var fileNameTextView: TextView

    override fun getLayoutId(): Int {
        return R.layout.activity_manage_affirmation
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Gestion des Insets
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

        // Récupérer le chemin du fichier depuis l’intent
        filePath = intent.getStringExtra("filePath") ?: run {
            Toast.makeText(this, "Chemin du fichier non trouvé.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialisation des vues
        fileNameTextView = findViewById(R.id.fileNameTextView)
        downloadButton = findViewById(R.id.downloadButton)
        renameButton = findViewById(R.id.renameButton)
        deleteButton = findViewById(R.id.deleteButton)
        backButton = findViewById(R.id.backButton)

        // Afficher le nom du fichier sans extension
        fileNameTextView.text = File(filePath).nameWithoutExtension

        // ─── 2. Préparer ExoPlayer ───
        val playerView = findViewById<PlayerView>(R.id.player_view2).apply {
            useController = true
            controllerShowTimeoutMs = 0
            showController()
            bringToFront()
            // À ce stade, exoPlayer est null, on l’assignera ci-dessous
        }

        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            playerView.player = player
            // On charge le fichier local dans ExoPlayer
            player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(filePath))))
            player.prepare()   // BUFFERING → READY
            player.playWhenReady = true
        }

        // ─── 3. Download Button ───
        downloadButton.setOnClickListener {
            // Avant toute navigation ou autre action, on ne coupe pas la lecture.
            // On ne fait rien ici : Télécharger ne quitte pas l’écran.
            val fileToDownload = File(filePath)
            if (fileToDownload.exists()) {
                downloadFile(fileToDownload)
            } else {
                Toast.makeText(this, "Fichier introuvable pour le téléchargement.", Toast.LENGTH_SHORT).show()
            }
        }

        // ─── 4. Renommer ───
        renameButton.setOnClickListener {
            // Comme on reste sur la même activité pour renommer, pas besoin d’arrêter la musique ici
            showRenameDialog(File(filePath))
        }

        // ─── 5. Supprimer ───
        deleteButton.setOnClickListener {
            // Afficher la boîte de confirmation
            val alertDialog = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_confirmation, null)
            alertDialog.setView(dialogView)

            val alert = alertDialog.create().apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_background)
            }
            alert.show()

            // Bouton Confirmer dans le dialogue
            dialogView.findViewById<Button>(R.id.confirmButton)?.setOnClickListener {
                val file = File(filePath)
                val deleted = file.delete()
                if (deleted) {
                    Toast.makeText(this, "${file.name} supprimé.", Toast.LENGTH_SHORT).show()
                    // → Avant de quitter pour retourner à mesAffirmationsDetails, on coupe la lecture
                    stopAndReleasePlayer()
                    val intent = Intent(this, mesAffirmationsDetails::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Erreur lors de la suppression.", Toast.LENGTH_SHORT).show()
                }
                alert.dismiss()
            }

            // Vérification permission écriture (Android < Q)
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1001)
            }

            // Bouton Annuler dans le dialogue
            dialogView.findViewById<Button>(R.id.cancelButton)?.setOnClickListener {
                alert.dismiss()
            }
        }

        // ─── 6. Bouton Retour (navigation interne) ───
        backButton.setOnClickListener {
            // Avant de quitter cet écran, on coupe la musique
            stopAndReleasePlayer()
            val intent = Intent(this, mesAffirmations::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Télécharge le fichier audio dans le dossier Téléchargements
     */
    private fun downloadFile(file: File) {
        // Vérifier si le stockage externe est disponible
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Toast.makeText(this, "Stockage externe non disponible.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android Q+ : MediaStore
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, file.name)
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "audio/mpeg")
                    put(android.provider.MediaStore.Downloads.RELATIVE_PATH, "Download")
                }
                val resolver = contentResolver
                val downloadsUri = android.provider.MediaStore.Downloads.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(downloadsUri, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { output ->
                        file.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    Toast.makeText(this, "Fichier téléchargé dans Téléchargements.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Erreur lors de l'insertion dans MediaStore.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Android < Q : copie manuelle dans /Download
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, file.name)
                file.copyTo(destFile, overwrite = true)
                Toast.makeText(this, "Fichier téléchargé : ${destFile.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Erreur lors du téléchargement : ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ManageAffirmationActivity", "Erreur : ${e.message}")
        }
    }

    /**
     * Affiche une boîte de dialogue pour renommer le fichier.
     */
    private fun showRenameDialog(file: File) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rename_file, null)
        val editText = dialogView.findViewById<EditText>(R.id.renameEditText)
        editText.setText(file.nameWithoutExtension)

        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        val alertDialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create().apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_background)
            }

        confirmButton.setOnClickListener {
            val newName = editText.text.toString().trim()
            if (newName.isNotEmpty()) {
                val newFile = File(file.parent, "$newName.mp3")
                if (file.renameTo(newFile)) {
                    Toast.makeText(this, "Fichier renommé en $newName.mp3", Toast.LENGTH_SHORT).show()
                    filePath = newFile.absolutePath
                    fileNameTextView.text = newFile.nameWithoutExtension
                } else {
                    Toast.makeText(this, "Erreur lors du renommage.", Toast.LENGTH_SHORT).show()
                }
                alertDialog.dismiss()
            } else {
                Toast.makeText(this, "Le nom ne peut pas être vide.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    /**
     * Coupe et libère ExoPlayer.
     */
    private fun stopAndReleasePlayer() {
        exoPlayer?.let { player ->
            player.stop()
            player.release()
        }
        exoPlayer = null
    }

    /**
     * Lorsque l’utilisateur appuie sur BACK physique, on coupe la musique puis on revient.
     */
    override fun onBackPressed() {
        stopAndReleasePlayer()
        super.onBackPressed()
    }

    /**
     * Ne jamais arrêter ExoPlayer dans onPause()/onStop() : ainsi la musique continue si l’app passe en arrière-plan ou si
     * l’écran se verrouille.
     */
    override fun onPause() {
        super.onPause()
        // → Ne rien faire ici
    }

    override fun onStop() {
        super.onStop()
        // → Ne rien faire ici
    }

    /**
     * Avant de détruire l’activité, s’assurer que le player est bien relâché pour éviter les fuites.
     */
    override fun onDestroy() {
        super.onDestroy()
        stopAndReleasePlayer()
    }
}
