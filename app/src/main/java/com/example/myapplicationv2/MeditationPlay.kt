package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MeditationPlay : Base() {  // Hérite de Base au lieu de AppCompatActivity

    // MediaPlayer Principal
    private var mediaPlayer: MediaPlayer? = null

    // UI Elements
    private lateinit var progressBar: ProgressBar
    private lateinit var pauseButton: ImageButton
    private lateinit var btnOK: Button
    private lateinit var editText: EditText

    // PopupWindow pour afficher le texte tapé
    private lateinit var popupWindow: PopupWindow

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

    companion object {
        private const val FADE_OUT_DURATION_SECONDS = 20 // 20 secondes
        private const val AFFIRMATION_DELAY_SECONDS = 10   // 10 secondes
        private const val INTRO_DELAY_MS = 3000L           // 3 secondes (peut être ajusté si nécessaire)
    }

    override fun getLayoutId(): Int {
        return R.layout.meditation_play  // Utilisez le layout adapté
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Configuration des Insets UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Initialisation des éléments UI existants
        progressBar = findViewById(R.id.progressBar)
        pauseButton = findViewById(R.id.imageButtonPause)
        btnOK = findViewById(R.id.buttonOkMeditation)
        editText = findViewById(R.id.nameAffirm)

        // Initialisation des vues Overlay
        circularProgressContainer = findViewById(R.id.circularProgressContainer)
        circularProgressIndicator = findViewById(R.id.circularProgressIndicator)
        circularProgressText = findViewById(R.id.circularProgressText)

        // --- Début de l'ajout du PopupWindow pour l'EditText ---
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate le layout de la pop-up (défini dans res/layout/popup_view.xml)
        val popupView = inflater.inflate(R.layout.popup_view, null)
        // Créer le PopupWindow en spécifiant la taille fixe (316dp x 74dp convertis en pixels)
        popupWindow = PopupWindow(
            popupView,
            dpToPx(316),
            dpToPx(74)
        ).apply {
            isOutsideTouchable = false
            isFocusable = false
        }
        // Récupère le TextView de la pop-up
        val popupText = popupView.findViewById<TextView>(R.id.popupText)

        // Met à jour le texte du popup en temps réel
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                popupText.text = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Affiche le popup quand l'EditText reçoit le focus et le ferme quand il le perd
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Ajustez l'offset pour positionner correctement la pop-up
                popupWindow.showAsDropDown(editText, 350, -editText.height - 300)
            } else {
                popupWindow.dismiss()
            }
        }
        // --- Fin de l'ajout du PopupWindow ---

        // Listener pour le bouton OK
        btnOK.setOnClickListener {
            val intent = Intent(this, Advices::class.java)
            val name = if (!editText.text.isNullOrEmpty()) editText.text.toString() else "affirmation"

            // Chemins source et destination
            val sourceFile = File(filesDir, "final_audio.mp3")
            val destinationDir = File(filesDir, "affirmation")

            // Créer le dossier "affirmation" s'il n'existe pas
            if (!destinationDir.exists()) {
                val created = destinationDir.mkdirs()
                if (!created) {
                    Log.e("MeditationPlay", "Impossible de créer le dossier affirmation")
                    Toast.makeText(this, "Erreur lors de la création du dossier affirmation.", Toast.LENGTH_SHORT).show()
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
                if (sourceFile.exists()) {
                    sourceFile.copyTo(destinationFile, overwrite = false)
                    Log.d("MeditationPlay", "Fichier enregistré dans le dossier affirmation : ${destinationFile.absolutePath}")
                    Toast.makeText(this, "Fichier copié sous le nom ${destinationFile.name} dans le dossier affirmation.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("MeditationPlay", "Le fichier source n'existe pas : ${sourceFile.absolutePath}")
                    Toast.makeText(this, "Le fichier source est introuvable.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MeditationPlay", "Erreur lors de la copie du fichier : ${e.message}")
                Toast.makeText(this, "Erreur lors de la copie du fichier.", Toast.LENGTH_SHORT).show()
            }

            // Lancer l'activité suivante
            startActivity(intent)
        }

        // Copie des ressources brutes vers le stockage interne
        val bowlStartFilePath = copyRawResourceToInternalStorage(R.raw.boltibetainson, "boltibetainson_start.mp3")
        val bowlEndFilePath = copyRawResourceToInternalStorage(R.raw.boltibetainson, "boltibetainson_end.mp3")

        if (bowlStartFilePath == null || bowlEndFilePath == null) {
            Toast.makeText(this, "Erreur lors de la copie des fichiers audio du bol tibétain.", Toast.LENGTH_SHORT).show()
            Log.e("MeditationPlay", "Échec de la copie des fichiers audio du bol tibétain.")
            return
        }

        // Récupération des données de l'intent
        val filePaths = intent.getStringExtra("filePaths")
        val selectedDurationInSeconds = intent.getIntExtra("selectedDuration", 0)
        val currentVoice = intent.getStringExtra("curentVoice")
        val userTexts = intent.getStringArrayListExtra("userTexts")?.distinct()?.toCollection(ArrayList())

        userTexts?.forEach { text ->
            Log.d("MeditationPlay", "Received text: $text")
        } ?: Log.d("MeditationPlay", "No texts received")

        Log.d("MeditationPlay", "Chemin de la musique choisie: $filePaths")
        Log.d("MeditationPlay", "Selected duration: $selectedDurationInSeconds seconds")

        val isIntroEnabled = intent.getBooleanExtra("isIntroEnabled", false)
        var introFilePath: String? = null

        if (isIntroEnabled) {
            introFilePath = copyRawResourceToInternalStorage(R.raw.intro, "intro.mp3")
            if (introFilePath == null) {
                Toast.makeText(this, "Erreur lors de la copie du fichier d'intro.", Toast.LENGTH_SHORT).show()
                Log.e("MeditationPlay", "Échec de la copie du fichier d'intro.")
            }
        }

        if (filePaths != null) {
            copyExternalMusic(filePaths, "recorded_music.mp3") { copySuccess ->
                if (copySuccess) {
                    runOnUiThread {
                        findViewById<ImageView>(R.id.imageView4).setImageResource(R.drawable.logo_final_nb)
                        showOverlay()
                    }
                    val finalOutputPath = "${filesDir.absolutePath}/final_audio.mp3"
                    mixAudioFilesWithFade(
                        bowlStartPath = bowlStartFilePath,
                        musicPath = "${filesDir.absolutePath}/recorded_music.mp3",
                        bowlEndPath = bowlEndFilePath,
                        affirmationPaths = userTexts ?: emptyList(),
                        outputPath = finalOutputPath,
                        selectedDurationInSeconds = selectedDurationInSeconds,
                        introPath = introFilePath,
                        callback = { mixSuccess ->
                            if (mixSuccess) {
                                playMainAudio(finalOutputPath, selectedDurationInSeconds)
                            } else {
                                runOnUiThread {
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
            Toast.makeText(this, "Chemin de la musique choisi est invalide.", Toast.LENGTH_SHORT).show()
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

    // Fonction utilitaire pour convertir dp en pixels
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // --- Méthodes existantes (copyRawResourceToInternalStorage, saveFileToInternalStorage, copyExternalMusic, etc.) ---

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

    private fun saveFileToInternalStorage(uri: Uri, fileName: String): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(filesDir, fileName)
        FileOutputStream(file).use { output ->
            inputStream?.use { input ->
                val buffer = ByteArray(4 * 1024)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        }
        return file.absolutePath
    }

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
                    val buffer = ByteArray(4 * 1024)
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

    private fun showOverlay() {
        runOnUiThread {
            circularProgressContainer.visibility = View.VISIBLE
            circularProgressIndicator.progress = 0
            circularProgressText.text = "0%"
        }
    }

    private fun hideOverlay() {
        runOnUiThread {
            circularProgressContainer.visibility = View.GONE
        }
    }

    private fun mixAudioFilesWithFade(
        bowlStartPath: String,
        musicPath: String,
        bowlEndPath: String,
        affirmationPaths: List<String>,
        outputPath: String,
        selectedDurationInSeconds: Int,
        introPath: String?,
        callback: (Boolean) -> Unit
    ) {
        showOverlay()
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

        val bowlStartDurationSeconds = getAudioDurationSeconds(bowlStartPath)
        val bowlEndDurationSeconds = getAudioDurationSeconds(bowlEndPath)
        val loopedMusicDuration = selectedDurationInSeconds

        Log.d("MeditationPlay", "Bowl Start Duration: $bowlStartDurationSeconds sec")
        Log.d("MeditationPlay", "Bowl End Duration: $bowlEndDurationSeconds sec")
        Log.d("MeditationPlay", "Looped Music Duration: $loopedMusicDuration sec")

        val potentialAffirmationCount = selectedDurationInSeconds / AFFIRMATION_DELAY_SECONDS
        val filterComplexBuilder = StringBuilder()

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

        val affIndices = mutableListOf<String>()
        for (i in 0 until potentialAffirmationCount) {
            val affirmationStartTime = AFFIRMATION_DELAY_SECONDS * (i + 1) + (if (introPath != null) getAudioDurationSeconds(introPath).toInt() else 0)
            if (selectedDurationInSeconds - affirmationStartTime < 10) {
                Log.d("MeditationPlay", "Affirmation $i ignorée : moins de 10 sec avant la fin")
                continue
            }
            val delayMs = affirmationStartTime * 1000
            val inputIndex = if (introPath != null) 4 + i else 3 + i
            filterComplexBuilder.append("[$inputIndex:a]atrim=duration=5,asetpts=PTS-STARTPTS,adelay=${delayMs}|${delayMs}[aff_delayed_$i]; ")
            affIndices.add("[aff_delayed_$i]")
        }

        var mixStreams = "[mix1]"
        for (tag in affIndices) {
            mixStreams += tag
        }
        if (affIndices.isNotEmpty()) {
            filterComplexBuilder.append("$mixStreams amix=inputs=${2 + affIndices.size}:duration=longest[aout]; ")
        } else {
            filterComplexBuilder.append("[mix1]anull[aout]; ")
        }

        val bowlEndDelayMs = ((loopedMusicDuration - bowlEndDurationSeconds) * 1000).toInt()
        filterComplexBuilder.append("[2:a]atrim=duration=$bowlEndDurationSeconds,asetpts=PTS-STARTPTS,adelay=${bowlEndDelayMs}|${bowlEndDelayMs}[bowl_end_delayed]; ")
        filterComplexBuilder.append("[aout][bowl_end_delayed]amix=inputs=2:duration=longest[afinal]; ")
        filterComplexBuilder.append("[afinal]loudnorm=I=-16:TP=-1.5:LRA=11[aout_normalized]")
        val filterComplex = filterComplexBuilder.toString()

        val ffmpegCommandBuilder = StringBuilder()
        ffmpegCommandBuilder.append("-y ")
        ffmpegCommandBuilder.append("-i \"$bowlStartPath\" ")
        ffmpegCommandBuilder.append("-stream_loop -1 -i \"$musicPath\" ")
        ffmpegCommandBuilder.append("-i \"$bowlEndPath\" ")
        if (introPath != null) {
            ffmpegCommandBuilder.append("-i \"$introPath\" ")
        }
        val totalAffirmations = potentialAffirmationCount
        for (i in 0 until totalAffirmations) {
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
                    findViewById<ImageView>(R.id.imageView4).setImageResource(R.drawable.logo_my_affirmation_tete_et_texte_vert)
                    hideOverlay()
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

    private fun getAudioDurationSeconds(filePath: String): Float {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toFloatOrNull()?.div(1000f) ?: 0f
        } catch (e: Exception) {
            Log.e("MeditationPlay", "Error retrieving duration for $filePath: ${e.message}")
            0f
        } finally {
            retriever.release()
        }
    }

    private fun playMainAudio(finalAudioPath: String?, durationTime: Int) {
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
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(it)
                    prepare()
                    setVolume(0.6f, 0.6f)
                }
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
                    mainHandler.postDelayed(this, 1000)
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

    private fun resumeAllMediaPlayers() {
        mediaPlayer?.start()
    }

    private fun pauseAllMediaPlayers() {
        mediaPlayer?.pause()
    }

    private fun stopAllAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            mediaPlayer = null
        }
        mainHandler.removeCallbacksAndMessages(null)
        isTrackingProgress = false
        hideOverlay()
        runOnUiThread {
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
