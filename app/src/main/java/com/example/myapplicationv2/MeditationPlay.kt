package com.example.myapplicationv2

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

class MeditationPlay : AppCompatActivity() {

    // MediaPlayer Principal
    private var mediaPlayer: MediaPlayer? = null

    // UI Elements
    private lateinit var progressBar: ProgressBar
    private lateinit var pauseButton: ImageButton

    // Handlers
    private val mainHandler = Handler(Looper.getMainLooper())

    // State
    private var isPaused = false

    // Constants
    companion object {
        private const val FADE_IN_DURATION_SECONDS = 5 // 5 secondes
        private const val FADE_OUT_DURATION_SECONDS = 20 // 20 secondes
        private const val AFFIRMATION_DELAY_SECONDS = 10 // 10 secondes
        private const val INTRO_DELAY_MS = 3000L // 3 secondes (peut être ajusté si nécessaire)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.meditation_play)

        // Configuration des Insets UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialisation des éléments UI
        progressBar = findViewById(R.id.progressBar)
        pauseButton = findViewById(R.id.imageButtonPause)

        // Copie des ressources brutes vers le stockage interne
        val bowlStartFilePath = copyRawResourceToInternalStorage(R.raw.boltibetainson, "boltibetainson_start.mp3")
        val bowlEndFilePath = copyRawResourceToInternalStorage(R.raw.boltibetainson, "boltibetainson_end.mp3")

        // Vérification de la copie des fichiers audio
        if (bowlStartFilePath == null || bowlEndFilePath == null) {
            Toast.makeText(this, "Erreur lors de la copie des fichiers audio du bol tibétain.", Toast.LENGTH_SHORT).show()
            Log.e("MeditationPlay", "Échec de la copie des fichiers audio du bol tibétain.")
            return
        }

        // Récupération des données de l'intent
        val filePaths = intent.getStringExtra("filePaths") // Chemin vers la musique sélectionnée
        val selectedDurationInSeconds = intent.getIntExtra("selectedDuration", 0)
        val currentVoice = intent.getStringExtra("curentVoice")
        val userTexts = intent.getStringArrayListExtra("userTexts")?.distinct()?.toCollection(ArrayList())

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
                Toast.makeText(this, "Erreur lors de la copie du fichier d'intro.", Toast.LENGTH_SHORT).show()
                Log.e("MeditationPlay", "Échec de la copie du fichier d'intro.")
                // Optionnellement, continuer sans l'intro
            }
        }

        if (filePaths != null) { // Vérification que filePaths n'est pas null
            // Copier la musique sélectionnée dans le stockage interne
            copyExternalMusic(filePaths, "recorded_music.mp3") { copySuccess ->
                if (copySuccess) {
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
                            }
                        }
                    )
                }
            }
        } else {
            Log.e("MeditationPlay", "Chemin de la musique choisi est null.")
            Toast.makeText(this, "Chemin de la musique choisi est invalide.", Toast.LENGTH_SHORT).show()
        }

        // Configuration du listener pour le bouton pause
        setupPauseButton()
    }

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
     * Mixe le son du bol tibétain, de la musique principale, et des affirmations avec fade-in et fade-out,
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
        introPath: String?, // Chemin de l'intro (peut être null)
        callback: (Boolean) -> Unit
    ) {
        // Vérifier que les fichiers existent
        val bowlStartFile = File(bowlStartPath)
        val musicFile = File(musicPath)
        val bowlEndFile = File(bowlEndPath)

        if (!bowlStartFile.exists()) {
            Log.e("MeditationPlay", "Fichier du bol tibétain de début introuvable: $bowlStartPath")
            runOnUiThread {
                Toast.makeText(this, "Fichier du bol tibétain de début introuvable.", Toast.LENGTH_SHORT).show()
            }
            callback(false)
            return
        }

        if (!musicFile.exists()) {
            Log.e("MeditationPlay", "Fichier de musique introuvable: $musicPath")
            runOnUiThread {
                Toast.makeText(this, "Fichier de musique introuvable.", Toast.LENGTH_SHORT).show()
            }
            callback(false)
            return
        }

        if (!bowlEndFile.exists()) {
            Log.e("MeditationPlay", "Fichier du bol tibétain de fin introuvable: $bowlEndPath")
            runOnUiThread {
                Toast.makeText(this, "Fichier du bol tibétain de fin introuvable.", Toast.LENGTH_SHORT).show()
            }
            callback(false)
            return
        }

        // Obtenir la durée des bols et de la musique
        val bowlStartDurationSeconds = getAudioDurationSeconds(bowlStartPath)
        val bowlEndDurationSeconds = getAudioDurationSeconds(bowlEndPath)
        val musicDurationSeconds = getAudioDurationSeconds(musicPath)

        Log.d("MeditationPlay", "Bowl Start Duration: $bowlStartDurationSeconds seconds")
        Log.d("MeditationPlay", "Bowl End Duration: $bowlEndDurationSeconds seconds")
        Log.d("MeditationPlay", "Music Duration: $musicDurationSeconds seconds")

        // Calculer la durée de la musique bouclée
        val loopedMusicDuration = selectedDurationInSeconds

        Log.d("MeditationPlay", "Looped Music Duration: $loopedMusicDuration seconds")

        // Calculer la durée des affirmations
        val affirmationIntervalSeconds = AFFIRMATION_DELAY_SECONDS  // 10 secondes
        val numberOfAffirmations = selectedDurationInSeconds / affirmationIntervalSeconds

        Log.d("MeditationPlay", "Number of affirmations: $numberOfAffirmations")

        // Limiter le nombre d'affirmations en fonction de la liste disponible
        val limitedNumberOfAffirmations = if (affirmationPaths.isNotEmpty()) {
            numberOfAffirmations
        } else {
            0
        }

        Log.d("MeditationPlay", "Limited Number of affirmations: $limitedNumberOfAffirmations")

        // Calculer la durée de l'intro si présente
        val introDurationSeconds = if (introPath != null) getAudioDurationSeconds(introPath) else 0f
        Log.d("MeditationPlay", "Intro Duration: $introDurationSeconds seconds")

        // Construire le filtre complexe
        val filterComplexBuilder = StringBuilder()

        // Ajouter l'intro si disponible
        if (introPath != null) {
            // Input indices:
            // 0: bowl_start
            // 1: music_looped
            // 2: bowl_end
            // 3: intro
            // 4+: affirmations

            // Appliquer une réduction de volume uniquement à la musique
            filterComplexBuilder.append("[1:a]volume=0.125[music_scaled]; ") // music_looped (réduction supplémentaire de 6dB)

            // Appliquer le fade-in et fade-out à la musique
            filterComplexBuilder.append("[music_scaled]afade=t=in:st=0:d=$FADE_IN_DURATION_SECONDS,afade=t=out:st=${loopedMusicDuration - FADE_OUT_DURATION_SECONDS}:d=$FADE_OUT_DURATION_SECONDS[music_faded]; ")

            // Concaténer bowl_start et intro sans réduction de volume
            filterComplexBuilder.append("[0:a][3:a]concat=n=2:v=0:a=1[start_intro]; ")

            // Mixer bowl_start + intro avec musique fadeée
            filterComplexBuilder.append("[start_intro][music_faded]amix=inputs=2:duration=longest[mix1]; ")
        } else {
            // Input indices sans intro:
            // 0: bowl_start
            // 1: music_looped
            // 2: bowl_end
            // 3+: affirmations

            // Appliquer une réduction de volume uniquement à la musique
            filterComplexBuilder.append("[1:a]volume=0.125[music_scaled]; ") // music_looped (réduction supplémentaire de 6dB)

            // Appliquer le fade-in et fade-out à la musique
            filterComplexBuilder.append("[music_scaled]afade=t=in:st=0:d=$FADE_IN_DURATION_SECONDS,afade=t=out:st=${loopedMusicDuration - FADE_OUT_DURATION_SECONDS}:d=$FADE_OUT_DURATION_SECONDS[music_faded]; ")

            // Mixer bowl_start avec musique fadeée sans réduction de volume
            filterComplexBuilder.append("[0:a][music_faded]amix=inputs=2:duration=longest[mix1]; ")
        }

        // Ajouter les affirmations avec delay et maintien du volume original
        for (i in 0 until limitedNumberOfAffirmations) {
            val affirmationPath = affirmationPaths[i % affirmationPaths.size] // Boucler si nécessaire

            // Calculer le délai en fonction de l'intro
            val delaySeconds = AFFIRMATION_DELAY_SECONDS * (i + 1) + introDurationSeconds.toInt()
            val delayMs = delaySeconds * 1000

            // Calculer l'index des inputs pour les affirmations
            val inputIndex = if (introPath != null) 4 + i else 3 + i

            // Appliquer le trim et le delay sans réduire le volume
            filterComplexBuilder.append("[$inputIndex:a]atrim=duration=5,asetpts=PTS-STARTPTS,adelay=${delayMs}|${delayMs}[aff_delayed_$i]; ")
        }

        // Construire la liste des streams à mixer
        var mixStreams = "[mix1]"
        for (i in 0 until limitedNumberOfAffirmations) {
            mixStreams += "[aff_delayed_$i]"
        }

        // Mixer toutes les affirmations avec le mix principal
        if (limitedNumberOfAffirmations > 0) {
            filterComplexBuilder.append("$mixStreams amix=inputs=${2 + limitedNumberOfAffirmations}:duration=longest[aout]; ")
        } else {
            filterComplexBuilder.append("[mix1]anull[aout]; ")
        }

        // Ajouter bowl_end avec delay
        // Calculer le délai pour bowl_end afin qu'il soit joué à la fin
        val bowlEndDelayMs = (loopedMusicDuration - bowlEndDurationSeconds) * 1000

        // Appliquer le trim et le delay à bowl_end sans réduire le volume
        filterComplexBuilder.append("[2:a]atrim=duration=$bowlEndDurationSeconds,asetpts=PTS-STARTPTS,adelay=${bowlEndDelayMs}|${bowlEndDelayMs}[bowl_end_delayed]; ")

        // Mixer bowl_end avec le mix final sans réduire le volume
        filterComplexBuilder.append("[aout][bowl_end_delayed]amix=inputs=2:duration=longest[afinal]; ")

        // Appliquer une normalisation globale
        filterComplexBuilder.append("[afinal]loudnorm=I=-16:TP=-1.5:LRA=11[aout_normalized]")

        val filterComplex = filterComplexBuilder.toString()

        // Construire la commande FFmpeg
        val ffmpegCommandBuilder = StringBuilder()
        ffmpegCommandBuilder.append("-y ")

        // Inputs
        ffmpegCommandBuilder.append("-i \"$bowlStartPath\" ") // Input 0: bowl_start
        ffmpegCommandBuilder.append("-stream_loop -1 -i \"$musicPath\" ") // Input 1: music_looped
        ffmpegCommandBuilder.append("-i \"$bowlEndPath\" ") // Input 2: bowl_end

        // Ajouter l'intro si disponible
        if (introPath != null) {
            ffmpegCommandBuilder.append("-i \"$introPath\" ") // Input 3: intro
        }

        // Ajouter les affirmations
        for (i in 0 until limitedNumberOfAffirmations) {
            val affirmationPath = affirmationPaths[i % affirmationPaths.size] // Boucler si nécessaire
            ffmpegCommandBuilder.append("-i \"$affirmationPath\" ") // Inputs 4+: affirmations si intro est activé, sinon 3+
        }

        // Filter complex
        ffmpegCommandBuilder.append("-filter_complex \"$filterComplex\" ")

        // Limiter la durée totale
        ffmpegCommandBuilder.append("-t \"$selectedDurationInSeconds\" ")

        // Mapping et encodage
        ffmpegCommandBuilder.append("-map \"[aout_normalized]\" ")
        ffmpegCommandBuilder.append("-c:a libmp3lame -b:a 192k ")
        ffmpegCommandBuilder.append("\"$outputPath\"")

        val ffmpegCommand = ffmpegCommandBuilder.toString()

        Log.d("MeditationPlay", "Executing FFmpeg command: $ffmpegCommand")

        // Exécuter la commande FFmpeg
        FFmpegKit.executeAsync(ffmpegCommand) { session ->
            val returnCode = session.returnCode
            val logs = session.allLogsAsString

            if (ReturnCode.isSuccess(returnCode)) {
                runOnUiThread {
                    Toast.makeText(this, "Mixage réussi.", Toast.LENGTH_SHORT).show()
                    Log.d("MeditationPlay", "Mixage réussi. Fichier final à: $outputPath")
                    callback(true)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Échec du mixage.", Toast.LENGTH_SHORT).show()
                    Log.e("MeditationPlay", "Échec du mixage: $logs")
                    callback(false)
                }
            }
        }
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
                    start()
                    setVolume(0.6f, 0.6f) // Volume réduit davantage pour compenser les ajustements de volume
                }

                // Configuration de la ProgressBar
                progressBar.max = durationTime
                progressBar.progress = 0

                // Démarrer le suivi de la progression
                startProgressTracking(durationTime)

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
     * Démarre le suivi de la progression de la lecture principale de l'audio.
     *
     * @param durationTime Durée de la lecture en secondes.
     */
    private fun startProgressTracking(durationTime: Int) {
        var remainingTime = durationTime

        mainHandler.post(object : Runnable {
            override fun run() {
                if (remainingTime > 0) {
                    remainingTime--
                    progressBar.progress = durationTime - remainingTime
                    mainHandler.postDelayed(this, 1000) // Mise à jour toutes les secondes

                    when (remainingTime) {
                        0 -> { // Fin de la lecture
                            Log.d("MeditationPlay", "Playback duration completed. Stopping audio.")
                            stopAllAudio()
                        }
                    }
                } else {
                    progressBar.progress = durationTime
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
