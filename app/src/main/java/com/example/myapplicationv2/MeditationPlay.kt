package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

class MeditationPlay : Base() {  // Hérite de Base au lieu de AppCompatActivity

    // MediaPlayer Principal
    private var mediaPlayer: MediaPlayer? = null

    // UI Elements
    private lateinit var progressBar: ProgressBar
    private lateinit var pauseButton: ImageButton
    private lateinit var btnOK: Button
    private lateinit var editText: EditText

    // Overlay UI Elements
    private lateinit var circularProgressContainer: View
    private lateinit var circularProgressIndicator: CircularProgressIndicator
    private lateinit var circularProgressText: TextView
    private var progressRunnable: Runnable? = null
    // Handler pour la mise à jour périodique
    private val mainHandler = Handler(Looper.getMainLooper())

    // États
    private var isPaused = true
    private var isTrackingProgress = false

    // Constants
    companion object {
        private const val FADE_OUT_DURATION_SECONDS = 20 // 20 secondes
        private const val AFFIRMATION_DELAY_SECONDS = 10 // 10 secondes
        private const val INTRO_DELAY_MS = 3000L // 3 secondes (peut être ajusté si nécessaire)
    }

    override fun getLayoutId(): Int {
        return R.layout.meditation_play  // Utilisez le layout adapté
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

        // Initialisation des éléments UI existants
        progressBar = findViewById(R.id.progressBar)
        pauseButton = findViewById(R.id.imageButtonPause)
        btnOK = findViewById(R.id.buttonOkMeditation)
        editText = findViewById(R.id.nameAffirm)

        // Initialisation des vues Overlay
        circularProgressContainer = findViewById(R.id.circularProgressContainer)
        circularProgressIndicator = findViewById(R.id.circularProgressIndicator)
        circularProgressText = findViewById(R.id.circularProgressText)

        // Configuration du listener pour le bouton OK
        btnOK.setOnClickListener {
            val intent = Intent(this, Advices::class.java)

            // Récupérer le nom saisi ou utiliser "affirmation" par défaut
            val name = if (!editText.text.isNullOrEmpty()) {
                editText.text.toString()
            } else {
                "affirmation"
            }

            // Chemins source et destination
            val sourceFile = File(filesDir, "final_audio.mp3")
            val destinationDir = File(filesDir, "affirmation")

            // Créer le dossier "affirmation" s'il n'existe pas
            if (!destinationDir.exists()) {
                val created = destinationDir.mkdirs()
                if (!created) {
                    Log.e("MeditationPlay", "Impossible de créer le dossier affirmation")
                    Toast.makeText(
                        this,
                        "Erreur lors de la création du dossier affirmation.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            // Générer un nom unique pour éviter d'écraser les fichiers existants
            var destinationFile = File(destinationDir, "$name.mp3")
            var counter = 1
            while (destinationFile.exists()) {
                destinationFile = File(destinationDir, "$name$counter.mp3")
                counter++
            }

            try {
                // Copier le fichier source dans le dossier de destination
                if (sourceFile.exists()) {
                    sourceFile.copyTo(destinationFile, overwrite = false)
                    Log.d(
                        "MeditationPlay",
                        "Fichier enregistré dans le dossier affirmation : ${destinationFile.absolutePath}"
                    )
                    Toast.makeText(
                        this,
                        "Fichier copié sous le nom ${destinationFile.name} dans le dossier affirmation.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(
                        "MeditationPlay",
                        "Le fichier source n'existe pas : ${sourceFile.absolutePath}"
                    )
                    Toast.makeText(this, "Le fichier source est introuvable.", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("MeditationPlay", "Erreur lors de la copie du fichier : ${e.message}")
                Toast.makeText(this, "Erreur lors de la copie du fichier.", Toast.LENGTH_SHORT)
                    .show()
            }

            // Lancer l'activité suivante
            startActivity(intent)
        }

        // Copie des ressources brutes vers le stockage interne
        val bowlStartFilePath =
            copyRawResourceToInternalStorage(R.raw.boltibetainson, "boltibetainson_start.mp3")
        val bowlEndFilePath =
            copyRawResourceToInternalStorage(R.raw.boltibetainson, "boltibetainson_end.mp3")

        // Vérification de la copie des fichiers audio
        if (bowlStartFilePath == null || bowlEndFilePath == null) {
            Toast.makeText(
                this,
                "Erreur lors de la copie des fichiers audio du bol tibétain.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("MeditationPlay", "Échec de la copie des fichiers audio du bol tibétain.")
            return
        }

        // Récupération des données de l'intent
        val filePaths = intent.getStringExtra("filePaths") // Chemin vers la musique sélectionnée
        val selectedDurationInSeconds = intent.getIntExtra("selectedDuration", 0)
        val currentVoice = intent.getStringExtra("curentVoice")
        val userTexts =
            intent.getStringArrayListExtra("userTexts")?.distinct()?.toCollection(ArrayList())

        // Journalisation des textes reçus
        userTexts?.forEach { text ->
            Log.d("MeditationPlay", "Received text: $text")
        } ?: Log.d("MeditationPlay", "No texts received")

        // Journalisation du chemin du fichier musique
        Log.d("MeditationPlay", "Chemin de la musique choisie: $filePaths")
        Log.d("MeditationPlay", "Selected duration: $selectedDurationInSeconds seconds")

        // Vérification si l'intro est activée et démarrage de la lecture en conséquence
        val isIntroEnabled = intent.getBooleanExtra("isIntroEnabled", false)
        var introFilePath: String? = null

        if (isIntroEnabled) {
            // Copier le fichier d'intro dans le stockage interne
            introFilePath = copyRawResourceToInternalStorage(R.raw.intro, "intro.mp3")
            if (introFilePath == null) {
                Toast.makeText(
                    this,
                    "Erreur lors de la copie du fichier d'intro.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("MeditationPlay", "Échec de la copie du fichier d'intro.")
                // Optionnellement, continuer sans l'intro
            }
        }

        if (filePaths != null) { // Vérification que filePaths n'est pas null
            // Copier la musique sélectionnée dans le stockage interne
            copyExternalMusic(filePaths, "recorded_music.mp3") { copySuccess ->
                if (copySuccess) {
                    runOnUiThread {
                        // Afficher le logo gris pendant le chargement
                        findViewById<ImageView>(R.id.imageView4).setImageResource(R.drawable.logo_final_nb)

                        // Afficher l'Overlay avec indicateur circulaire
                        showOverlay()
                    }

                    // Mixage du bol + musique avec fade et intégration des affirmations et intro
                    val finalOutputPath = "${filesDir.absolutePath}/final_audio.mp3"
                    mixAudioFilesWithFade(
                        bowlStartPath = bowlStartFilePath,
                        musicPath = "${filesDir.absolutePath}/recorded_music.mp3",
                        bowlEndPath = bowlEndFilePath,
                        affirmationPaths = userTexts ?: emptyList(), // Passer les affirmations
                        outputPath = finalOutputPath,
                        selectedDurationInSeconds = selectedDurationInSeconds,
                        introPath = introFilePath, // Passer l'intro (peut être null)
                        callback = { mixSuccess ->
                            if (mixSuccess) {
                                // Lecture de l'audio mixé final
                                playMainAudio(finalOutputPath, selectedDurationInSeconds)
                            } else {
                                // Masquer l'Overlay en cas d'échec
                                runOnUiThread {
                                    // Remplacer le logo gris par le logo normal
                                    findViewById<ImageView>(R.id.imageView4).setImageResource(R.drawable.logo_my_affirmation_tete_et_texte_vert)

                                    hideOverlay()
                                }
                            }
                        }
                    )
                }
            }

        } else {
            Log.e("MeditationPlay", "Chemin de la musique choisi est null.")
            Toast.makeText(this, "Chemin de la musique choisi est invalide.", Toast.LENGTH_SHORT)
                .show()
        }

        pauseButton.setOnClickListener {
            if (isPaused) {
                pauseButton.setImageResource(R.drawable.imgunpause)
                mediaPlayer?.start()
                isPaused = false
            } else {
                mediaPlayer?.pause()
                pauseButton.setImageResource(R.drawable.imgpause)
                isPaused = true

            }

        }
    }
    // Fonctions existantes

    /**
     * Copie une ressource brute dans le stockage interne et retourne son chemin absolu.
     */
    private fun copyRawResourceToInternalStorage(resourceId: Int, fileName: String): String? {
        return try {
            val uri = Uri.parse("android.resource://${packageName}/$resourceId")
            val filePath = saveFileToInternalStorage(uri, fileName)
            Log.d("MeditationPlay", "File $fileName saved at $filePath")
            filePath
        } catch (e: Exception) {
            Log.e("MeditationPlay", "Error copying resource $fileName: ${e.message}")
            null
        }
    }

    /**
     * Sauvegarde un fichier depuis un URI dans le stockage interne.
     */
    private fun saveFileToInternalStorage(uri: Uri, fileName: String): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(filesDir, fileName)
        FileOutputStream(file).use { output ->
            inputStream?.use { input ->
                val buffer = ByteArray(4 * 1024) // Buffer de 4KB
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        }
        return file.absolutePath
    }

    /**
     * Copie un fichier de musique externe dans le stockage interne.
     *
     * @param sourcePath Chemin source du fichier de musique externe.
     * @param destFileName Nom du fichier de destination dans le stockage interne.
     * @param callback Fonction de rappel pour indiquer le succès ou l'échec de la copie.
     */
    private fun copyExternalMusic(sourcePath: String, destFileName: String, callback: (Boolean) -> Unit) {
        try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                Log.e("MeditationPlay", "Fichier de musique externe introuvable: $sourcePath")
                runOnUiThread {
                    Toast.makeText(this, "Fichier de musique externe introuvable.", Toast.LENGTH_SHORT).show()
                }
                callback(false)
                return
            }

            val destFile = File(filesDir, destFileName)
            FileOutputStream(destFile).use { output ->
                sourceFile.inputStream().use { input ->
                    val buffer = ByteArray(4 * 1024) // Buffer de 4KB
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }
            Log.d("MeditationPlay", "Musique externe copiée à: ${destFile.absolutePath}")
            callback(true)
        } catch (e: Exception) {
            Log.e("MeditationPlay", "Erreur lors de la copie de la musique externe: ${e.message}")
            runOnUiThread {
                Toast.makeText(this, "Erreur lors de la copie de la musique.", Toast.LENGTH_SHORT).show()
            }
            callback(false)
        }
    }

    /**
     * Affiche l'overlay avec l'indicateur de progression.
     */
    private fun showOverlay() {
        runOnUiThread {
            circularProgressContainer.visibility = View.VISIBLE
            circularProgressIndicator.progress = 0
            circularProgressText.text = "0%"
        }
    }

    /**
     * Masque l'overlay.
     */
    private fun hideOverlay() {
        runOnUiThread {
            circularProgressContainer.visibility = View.GONE
        }
    }

    /**
     * Mixe le son du bol tibétain, de la musique principale, et des affirmations avec fade-out,
     * ajoute le bol tibétain au début et à la fin de l'enregistrement, et ajuste la durée totale.
     *
     * @param bowlStartPath Chemin du fichier audio du bol tibétain (début).
     * @param musicPath Chemin du fichier audio de la musique principale.
     * @param bowlEndPath Chemin du fichier audio du bol tibétain (fin).
     * @param affirmationPaths Liste des chemins des fichiers audio des affirmations.
     * @param outputPath Chemin de sortie pour le fichier audio mixé.
     * @param selectedDurationInSeconds Durée totale sélectionnée en secondes.
     * @param introPath Chemin de l'intro (peut être null).
     * @param callback Fonction de rappel pour indiquer le succès ou l'échec du mixage.
     */
    private fun mixAudioFilesWithFade(
        bowlStartPath: String,
        musicPath: String,
        bowlEndPath: String,
        affirmationPaths: List<String>,
        outputPath: String,
        selectedDurationInSeconds: Int,
        introPath: String?, // Peut être null
        callback: (Boolean) -> Unit
    ) {
        showOverlay() // Afficher l'overlay avant de commencer le mixage

        // Vérifier que les fichiers existent
        val bowlStartFile = File(bowlStartPath)
        val musicFile = File(musicPath)
        val bowlEndFile = File(bowlEndPath)

        if (!bowlStartFile.exists() || !musicFile.exists() || !bowlEndFile.exists()) {
            runOnUiThread {
                Toast.makeText(this, "Un des fichiers audio est introuvable.", Toast.LENGTH_SHORT).show()
                hideOverlay()
            }
            callback(false)
            return
        }

        // Récupérer les durées
        val bowlStartDurationSeconds = getAudioDurationSeconds(bowlStartPath)
        val bowlEndDurationSeconds = getAudioDurationSeconds(bowlEndPath)
        val loopedMusicDuration = selectedDurationInSeconds

        Log.d("MeditationPlay", "Bowl Start Duration: $bowlStartDurationSeconds sec")
        Log.d("MeditationPlay", "Bowl End Duration: $bowlEndDurationSeconds sec")
        Log.d("MeditationPlay", "Looped Music Duration: $loopedMusicDuration sec")

        // Calculer le nombre d'affirmations potentielles (sans filtrer)
        val potentialAffirmationCount = selectedDurationInSeconds / AFFIRMATION_DELAY_SECONDS

        // Construire le filtre complexe
        val filterComplexBuilder = StringBuilder()

        // Si une intro est présente, on l'intègre
        if (introPath != null) {
            filterComplexBuilder.append("[1:a]volume=0.125[music_scaled]; ")
            filterComplexBuilder.append("[music_scaled]afade=t=out:st=${loopedMusicDuration - FADE_OUT_DURATION_SECONDS}:d=$FADE_OUT_DURATION_SECONDS[music_faded]; ")
            filterComplexBuilder.append("[0:a][3:a]concat=n=2:v=0:a=1[start_intro]; ")
            filterComplexBuilder.append("[start_intro][music_faded]amix=inputs=2:duration=longest[mix1]; ")
        } else {
            filterComplexBuilder.append("[1:a]volume=0.125[music_scaled]; ")
            filterComplexBuilder.append("[music_scaled]afade=t=out:st=${loopedMusicDuration - FADE_OUT_DURATION_SECONDS}:d=$FADE_OUT_DURATION_SECONDS[music_faded]; ")
            filterComplexBuilder.append("[0:a][music_faded]amix=inputs=2:duration=longest[mix1]; ")
        }

        // Préparer la liste des noms de streams d'affirmation réellement programmés
        val affIndices = mutableListOf<String>()
        for (i in 0 until potentialAffirmationCount) {
            // Calculer le temps de début prévu pour l'affirmation
            val affirmationStartTime = AFFIRMATION_DELAY_SECONDS * (i + 1) + (if (introPath != null) getAudioDurationSeconds(introPath).toInt() else 0)
            // Si le temps restant avant le gong de fin est inférieur à 10 secondes, on ignore cette affirmation
            if (selectedDurationInSeconds - affirmationStartTime < 10) {
                Log.d("MeditationPlay", "Affirmation $i ignorée : moins de 10 sec avant la fin")
                continue
            }
            // Calculer le délai en millisecondes
            val delayMs = affirmationStartTime * 1000
            // L'index d'entrée dans FFmpeg dépend de la présence de l'intro
            val inputIndex = if (introPath != null) 4 + i else 3 + i
            filterComplexBuilder.append("[$inputIndex:a]atrim=duration=5,asetpts=PTS-STARTPTS,adelay=${delayMs}|${delayMs}[aff_delayed_$i]; ")
            affIndices.add("[aff_delayed_$i]")
        }

        // Construire la liste des streams à mixer
        var mixStreams = "[mix1]"
        for (tag in affIndices) {
            mixStreams += tag
        }
        if (affIndices.isNotEmpty()) {
            filterComplexBuilder.append("$mixStreams amix=inputs=${2 + affIndices.size}:duration=longest[aout]; ")
        } else {
            filterComplexBuilder.append("[mix1]anull[aout]; ")
        }

        // Ajouter bowl_end avec delay
        val bowlEndDelayMs = ((loopedMusicDuration - bowlEndDurationSeconds) * 1000).toInt()
        filterComplexBuilder.append("[2:a]atrim=duration=$bowlEndDurationSeconds,asetpts=PTS-STARTPTS,adelay=${bowlEndDelayMs}|${bowlEndDelayMs}[bowl_end_delayed]; ")

        // Mixer bowl_end avec le mix final
        filterComplexBuilder.append("[aout][bowl_end_delayed]amix=inputs=2:duration=longest[afinal]; ")

        // Appliquer la normalisation
        filterComplexBuilder.append("[afinal]loudnorm=I=-16:TP=-1.5:LRA=11[aout_normalized]")

        val filterComplex = filterComplexBuilder.toString()

        // Construire la commande FFmpeg
        val ffmpegCommandBuilder = StringBuilder()
        ffmpegCommandBuilder.append("-y ")
        ffmpegCommandBuilder.append("-i \"$bowlStartPath\" ") // Input 0
        ffmpegCommandBuilder.append("-stream_loop -1 -i \"$musicPath\" ") // Input 1
        ffmpegCommandBuilder.append("-i \"$bowlEndPath\" ") // Input 2
        if (introPath != null) {
            ffmpegCommandBuilder.append("-i \"$introPath\" ") // Input 3
        }
        // Ajouter chaque affirmation programmée (les affirmations doivent être dans le même ordre que leur index utilisé)
        val totalAffirmations = potentialAffirmationCount // Notez : certaines affirmations peuvent être ignorées
        for (i in 0 until totalAffirmations) {
            // Même si on a ignoré certaines affirmations, nous devons ajouter les entrées correspondantes.
            // Cependant, vous pouvez choisir de n’ajouter que celles qui ne sont pas ignorées.
            // Ici, nous partons du principe que les affirmations ignorées ne sont pas ajoutées.
            if (!affIndices.contains("[aff_delayed_$i]")) continue
            ffmpegCommandBuilder.append("-i \"${affirmationPaths[i % affirmationPaths.size]}\" ")
        }

        ffmpegCommandBuilder.append("-filter_complex \"$filterComplex\" ")
        ffmpegCommandBuilder.append("-t \"$selectedDurationInSeconds\" ")
        ffmpegCommandBuilder.append("-map \"[aout_normalized]\" ")
        ffmpegCommandBuilder.append("-c:a libmp3lame -b:a 192k ")
        ffmpegCommandBuilder.append("\"$outputPath\"")

        val ffmpegCommand = ffmpegCommandBuilder.toString()

        Log.d("MeditationPlay", "Executing FFmpeg command: $ffmpegCommand")

        FFmpegKit.executeAsync(ffmpegCommand, { session ->
            val returnCode = session.returnCode
            val logs = session.allLogsAsString
            if (ReturnCode.isSuccess(returnCode)) {
                runOnUiThread {
                    Toast.makeText(this, "Mixage réussi. Fichier final à: $outputPath", Toast.LENGTH_SHORT).show()
                    hideOverlay()
                    // Vous pouvez mettre à jour d'autres éléments de l'interface ici.
                }
                callback(true)
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Échec du mixage.", Toast.LENGTH_SHORT).show()
                    Log.e("MeditationPlay", "Mixage échoué: $logs")
                    hideOverlay()
                }
                callback(false)
            }
        }, { log ->
            // Mise à jour du pourcentage de progression (si nécessaire)
            val timeRegex = Regex("time=(\\d+):(\\d+):(\\d+\\.\\d+)")
            timeRegex.find(log.message)?.let { matchResult ->
                val (hours, minutes, seconds) = matchResult.destructured
                val currentTimeSeconds = hours.toInt() * 3600 + minutes.toInt() * 60 + seconds.toFloat()
                val percentage = ((currentTimeSeconds / selectedDurationInSeconds) * 100).toInt().coerceIn(0, 100)
                runOnUiThread {
                    circularProgressIndicator.progress = percentage
                    circularProgressText.text = "$percentage%"
                    circularProgressText.contentDescription = "Progression de l'audio : $percentage%"
                }
                Log.d("MeditationPlay", "Progression FFmpeg: $percentage%")
            }
        }, { statistics ->
            // Callback pour statistiques supplémentaires (si nécessaire)
        })
    }

    /**
     * Récupère la durée d'un fichier audio en secondes.
     *
     * @param filePath Chemin vers le fichier audio.
     * @return Durée en secondes, ou 0f si impossible à récupérer.
     */
    private fun getAudioDurationSeconds(filePath: String): Float {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationStr?.toFloatOrNull()?.div(1000f) ?: 0f
        } catch (e: Exception) {
            Log.e("MeditationPlay", "Error retrieving duration for $filePath: ${e.message}")
            return 0f
        } finally {
            retriever.release()
        }
    }

    /**
     * Joue l'audio principal mixé avec suivi de progression.
     *
     * @param finalAudioPath Chemin du fichier audio mixé.
     * @param durationTime Durée de la lecture en secondes.
     */
    private fun playMainAudio(
        finalAudioPath: String?,
        durationTime: Int
    ) {
        finalAudioPath?.let {
            val mainFile = File(it)
            if (!mainFile.exists()) {
                Log.e("MeditationPlay", "Main audio file not found at $it")
                Toast.makeText(this, "Main audio file not found.", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d("MeditationPlay", "Final audio path: $finalAudioPath")
            Log.d("MeditationPlay", "Final audio exists: ${mainFile.exists()}")

            try {
                // Initialiser MediaPlayer
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(it)
                    prepare()
                    //start()
                    setVolume(0.6f, 0.6f)
                }

                // **Supprimer ou commenter l'appel à showOverlay()**
                /*
                runOnUiThread {
                    showOverlay()
                }
                */

                // Démarrer le suivi de la progression avec le cercle
                startProgressTrackingWithCircularIndicator(durationTime)

                mediaPlayer?.setOnCompletionListener {
                    Log.d("MeditationPlay", "Main audio playback completed.")
                    stopAllAudio()
                }
            } catch (e: Exception) {
                Log.e("MeditationPlay", "Error playing main audio: ${e.message}")
                Toast.makeText(this, "Error playing main audio.", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e("MeditationPlay", "Invalid main audio file path.")
            Toast.makeText(this, "Invalid main audio file path.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Démarre le suivi de la progression de la lecture principale de l'audio en utilisant le CircularProgressIndicator.
     *
     * @param durationTime Durée de la lecture en secondes.
     */
    private fun startProgressTrackingWithCircularIndicator(durationTime: Int) {
        if (isTrackingProgress) {
            Log.d("MeditationPlay", "Progress tracking already in progress. Skipping.")
            return
        }

        isTrackingProgress = true
        var remainingTime = durationTime

        mainHandler.post(object : Runnable {
            override fun run() {
                if (remainingTime > 0) {
                    remainingTime--
                    val currentProgress = durationTime - remainingTime
                    val percentage = ((currentProgress / durationTime.toFloat()) * 100).toInt().coerceIn(0, 100)

                    runOnUiThread {
                        circularProgressIndicator.progress = percentage
                        circularProgressText.text = "$percentage%"
                        circularProgressText.contentDescription = "Progression de l'audio : $percentage pour cent"
                    }

                    mainHandler.postDelayed(this, 1000) // Mise à jour toutes les secondes

                    if (remainingTime == 0) {
                        Log.d("MeditationPlay", "Playback duration completed. Stopping audio.")
                        stopAllAudio()
                    }
                } else {
                    runOnUiThread {
                        circularProgressIndicator.progress = 100
                        circularProgressText.text = "100%"
                        circularProgressText.contentDescription = "Progression de l'audio : 100 pour cent"
                    }
                    Log.d("MeditationPlay", "Playback duration completed. Stopping audio.")
                    stopAllAudio()
                }
            }
        })
    }

    /**
     * Configure le bouton pause pour basculer l'état de lecture.
     */
    private fun setupPauseButton() {
        pauseButton.setOnClickListener {
            if (isPaused) {
                resumeAllMediaPlayers()
                pauseButton.setImageResource(R.drawable.imgunpause)
                isPaused = false
                Log.d("MeditationPlay", "Playback resumed")
            } else {
                pauseAllMediaPlayers()
                pauseButton.setImageResource(R.drawable.imgpause)
                isPaused = true
                Log.d("MeditationPlay", "Playback paused")
            }
        }
    }

    /**
     * Reprend le MediaPlayer principal.
     */
    private fun resumeAllMediaPlayers() {
        mediaPlayer?.start()
    }

    /**
     * Met en pause le MediaPlayer principal.
     */
    private fun pauseAllMediaPlayers() {
        mediaPlayer?.pause()
    }

    /**
     * Arrête le MediaPlayer principal et supprime tous les Handlers.
     */
    private fun stopAllAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            mediaPlayer = null
        }

        // Supprimer tous les callbacks
        mainHandler.removeCallbacksAndMessages(null)
        isTrackingProgress = false

        // Masquer l'overlay et les autres éléments
        hideOverlay()

        runOnUiThread {
            // Masquer la ProgressBar si nécessaire
            progressBar.visibility = View.GONE
        }

        Log.d("MeditationPlay", "All audio players stopped and released.")
    }

    override fun onPause() {
        super.onPause()
        stopAllAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAllAudio()
    }
}
