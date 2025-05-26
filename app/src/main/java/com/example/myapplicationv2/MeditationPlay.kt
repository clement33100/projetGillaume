package com.example.myapplicationv2

import android.content.Context
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
import android.view.inputmethod.InputBinding
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
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
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView


class MeditationPlay : Base() {  // Hérite de Base au lieu de AppCompatActivity

    // MediaPlayer Principal
    private var mediaPlayer: MediaPlayer? = null
    private var exoPlayer: ExoPlayer? = null
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

    //lateinit var binding: ActivityMediaPlayerBinding


    companion object {
        private const val FADE_OUT_DURATION_SECONDS = 20 // 20 secondes
        private const val AFFIRMATION_DELAY_SECONDS = 10   // 10 secondes
        private const val FADE_IN_DURATION_SECONDS = 5
    }

    override fun getLayoutId(): Int {
        return R.layout.meditation_play  // Utilisez le layout adapté
    }


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val playerView = findViewById<PlayerView>(R.id.player_view).apply {
            useController = true
            controllerShowTimeoutMs = 0
            showController()
            bringToFront()
            player = exoPlayer
        }

    exoPlayer = ExoPlayer.Builder(this).build().also { exo ->

        exo.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                Log.d("DBG", "state=$state")              // 1=IDLE 2=BUFFERING 3=READY 4=ENDED
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d("DBG", "isPlaying=$isPlaying")
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("DBG", "Player error", error)
            }
        })

        exo.volume        = 0.6f          // réglages initiaux
        exo.playWhenReady = true          // démarrage auto après prepare()

        playerView.player = exo           // on attache la vue
    }



        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Gestion des insets système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        /* ----------------------------------------------------------------
           Initialisation des vues principales
           ---------------------------------------------------------------- */

        btnOK        = findViewById(R.id.buttonOkMeditation)
        editText     = findViewById(R.id.nameAffirm)

        /* ----------------------------------------------------------------
           Initialisation de l’overlay circulaire
           ---------------------------------------------------------------- */
        circularProgressContainer = findViewById(R.id.circularProgressContainer)
        circularProgressIndicator = findViewById(R.id.circularProgressIndicator)
        circularProgressText      = findViewById(R.id.circularProgressText)

        circularProgressContainer.visibility = View.GONE
        /* ----------------------------------------------------------------
           Bouton OK : copie/renommage du fichier et passage à l’activité suivante
           ---------------------------------------------------------------- */
        btnOK.setOnClickListener {
            val intent = Intent(this, Advices::class.java)
            val name = if (!editText.text.isNullOrEmpty()) editText.text.toString() else "affirmation"

            val sourceFile       = File(filesDir, "final_audio.mp3")
            val destinationDir   = File(filesDir, "affirmation")

            // création du dossier "affirmation" si nécessaire
            if (!destinationDir.exists()) {
                if (!destinationDir.mkdirs()) {
                    Log.e("MeditationPlay", "Impossible de créer le dossier affirmation")
                    //Toast.makeText(this, "Erreur lors de la création du dossier affirmation.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            var destinationFile = File(destinationDir, "$name.mp3")
            var counter = 1
            while (destinationFile.exists()) {
                destinationFile = File(destinationDir, "$name$counter.mp3")
                counter++
            }

            try {
                if (sourceFile.exists()) {
                    sourceFile.copyTo(destinationFile, overwrite = false)
                    Log.d("MeditationPlay", "Fichier enregistré : ${destinationFile.absolutePath}")
                    //Toast.makeText(this, "Fichier copié sous le nom ${destinationFile.name}.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("MeditationPlay", "Le fichier source n'existe pas : ${sourceFile.absolutePath}")
                    //Toast.makeText(this, "Le fichier source est introuvable.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MeditationPlay", "Erreur lors de la copie : ${e.message}")
                //Toast.makeText(this, "Erreur lors de la copie du fichier.", Toast.LENGTH_SHORT).show()
            }
            startActivity(intent)
        }

        /* ----------------------------------------------------------------
           Préparation des audios (bols, intro, musique, affirmations)
           ---------------------------------------------------------------- */
        val bowlStartFilePath = copyRawResourceToInternalStorage(R.raw.boltibetainson, "boltibetainson_start.mp3")
        val bowlEndFilePath   = copyRawResourceToInternalStorage(R.raw.boltibetainson, "boltibetainson_end.mp3")

        if (bowlStartFilePath == null || bowlEndFilePath == null) {
            //Toast.makeText(this, "Erreur lors de la copie des fichiers audio du bol tibétain.", Toast.LENGTH_SHORT).show()
            Log.e("MeditationPlay", "Échec de la copie des fichiers bol tibétain.")
            return
        }

        // Récupération des données de l’intent
        val filePaths                = intent.getStringExtra("filePaths")
        val selectedDurationInSeconds = intent.getIntExtra("selectedDuration", 0)
        val currentVoice             = intent.getStringExtra("curentVoice")
        val userTexts                = intent.getStringArrayListExtra("userTexts")?.distinct()?.toCollection(ArrayList())

        userTexts?.forEach { Log.d("MeditationPlay", "Received text: $it") }
            ?: Log.d("MeditationPlay", "No texts received")

        val isIntroEnabled = intent.getBooleanExtra("isIntroEnabled", false)
        var introFilePath: String? = null
        if (isIntroEnabled) {
            introFilePath = copyRawResourceToInternalStorage(R.raw.intromeditation, "intromeditation.mp3")
            if (introFilePath == null) {
                //Toast.makeText(this, "Erreur lors de la copie du fichier d'intro.", Toast.LENGTH_SHORT).show()
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
                        bowlStartPath            = bowlStartFilePath,
                        musicPath                = "${filesDir.absolutePath}/recorded_music.mp3",
                        bowlEndPath              = bowlEndFilePath,
                        affirmationPaths         = userTexts ?: emptyList(),
                        outputPath               = finalOutputPath,
                        selectedDurationInSeconds = selectedDurationInSeconds,
                        introPath                = introFilePath
                    ) { mixSuccess ->
                        if (mixSuccess) {
                            runOnUiThread {
                                playMainAudio(finalOutputPath, selectedDurationInSeconds)
                            }
                        } else {
                            runOnUiThread {
                                findViewById<ImageView>(R.id.imageView4)
                                    .setImageResource(R.drawable.logo_my_affirmation_tete_et_texte_vert)
                                hideOverlay()
                            }
                        }
                    }
                }
            }
        } else {
            Log.e("MeditationPlay", "Chemin de la musique choisi est null.")
            //Toast.makeText(this, "Chemin de la musique choisi est invalide.", Toast.LENGTH_SHORT).show()
        }

        /* ----------------------------------------------------------------
           Bouton pause/lecture
           ---------------------------------------------------------------- */
        /*pauseButton.setOnClickListener {
            if (isPaused) {
                pauseButton.setImageResource(R.drawable.imgunpause)
                mediaPlayer?.start()
                isPaused = false
            } else {
                mediaPlayer?.pause()
                pauseButton.setImageResource(R.drawable.imgpause)
                isPaused = true
            }
        }*/
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
                    //Toast.makeText(this, "Fichier de musique externe introuvable.", Toast.LENGTH_SHORT).show()
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
                //Toast.makeText(this, "Erreur lors de la copie de la musique.", Toast.LENGTH_SHORT).show()
            }
            callback(false)
        }
    }

    private fun showOverlay() {
        circularProgressContainer.apply {
            visibility  = View.VISIBLE
            isClickable = true       // bloque l’UI le temps du mixage
            //bringToFront()
        }
        circularProgressIndicator.progress = 0
        circularProgressText.text = "0%"
    }

    private fun hideOverlay() {
        circularProgressContainer.apply {
            visibility  = View.GONE  // retire complètement la vue du layout
            isClickable = false

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

        /* ───────────────────── Vérifications ───────────────────── */
        val bowlStartFile = File(bowlStartPath)
        val musicFile     = File(musicPath)
        val bowlEndFile   = File(bowlEndPath)
        if (!bowlStartFile.exists() || !musicFile.exists() || !bowlEndFile.exists()) {
            runOnUiThread {
                //Toast.makeText(this, "Un des fichiers audio est introuvable.", Toast.LENGTH_SHORT).show()
                hideOverlay()
            }
            callback(false); return
        }

        val bowlStartDurationSeconds = getAudioDurationSeconds(bowlStartPath)
        val bowlEndDurationSeconds   = getAudioDurationSeconds(bowlEndPath)
        val loopedMusicDuration      = selectedDurationInSeconds

        val potentialAffirmationCount = selectedDurationInSeconds / AFFIRMATION_DELAY_SECONDS
        val filterComplexBuilder      = StringBuilder()

        /* ───────────────────── Flux MUSIQUE (fade-in / fade-out) ───────────────────── */
        val musicFilter = """
        [1:a]volume=0.07,
             atrim=duration=$loopedMusicDuration,
             asetpts=PTS-STARTPTS[music_cut];
        [music_cut]afade=t=in:st=0:d=$FADE_IN_DURATION_SECONDS,
                   afade=t=out:st=${loopedMusicDuration - FADE_OUT_DURATION_SECONDS}:d=$FADE_OUT_DURATION_SECONDS[music_ready];
    """.trimIndent()

        if (introPath != null) {
            // Avec intro : bol de début = input 0, intro = input 3
            filterComplexBuilder.append(musicFilter).append(' ')
            filterComplexBuilder.append("[0:a][3:a]concat=n=2:v=0:a=1[start_intro]; ")
            filterComplexBuilder.append("[start_intro][music_ready]amix=inputs=2:duration=longest[mix1]; ")
        } else {
            // Sans intro : bol de début = input 0
            filterComplexBuilder.append(musicFilter).append(' ')
            filterComplexBuilder.append("[0:a][music_ready]amix=inputs=2:duration=longest[mix1]; ")
        }

        /* ───────────────────── Insertion des affirmations ───────────────────── */
        val affIndices = mutableListOf<String>()
        for (i in 0 until potentialAffirmationCount) {
            val startOffset = AFFIRMATION_DELAY_SECONDS * (i + 1) +
                    (introPath?.let { getAudioDurationSeconds(it).toInt() } ?: 0)
            if (selectedDurationInSeconds - startOffset < 10) break   // marge de fin

            val delayMs   = startOffset * 1000
            val inputIdx  = if (introPath != null) 4 + i else 3 + i
            filterComplexBuilder.append(
                "[$inputIdx:a]atrim=duration=5,asetpts=PTS-STARTPTS," +
                        "adelay=$delayMs|$delayMs[aff_delayed_$i]; "
            )
            affIndices += "[aff_delayed_$i]"
        }
        val totalInputs = 1 + affIndices.size          // 1 = [mix1]

        val musicWeight = 1f     // poids appliqué à la musique+bol
        val voiceWeight = 0.8f     // poids appliqué à chaque affirmation

// construit la chaîne "1|0.5|0.5|..."    ← mix1|aff0|aff1|...
        val weights = buildString {
            append(musicWeight)
            repeat(affIndices.size) { append("|$voiceWeight") }
        }

        val mixStreams = "[mix1]" + affIndices.joinToString("")
        if (affIndices.isNotEmpty()) {
            filterComplexBuilder.append(
                "$mixStreams amix=" +
                        "inputs=$totalInputs:" +
                        "duration=longest:" +
                        "normalize=0:" +
                        "dropout_transition=0:" +
                        "weights=$weights" +      // ← ICI : les voix > musique
                        "[aout]; "
            )
        } else {
            filterComplexBuilder.append("[mix1]anull[aout]; ")
        }

        /* ───────────────────── Bol de fin ───────────────────── */
        val bowlEndDelayMs = ((loopedMusicDuration - bowlEndDurationSeconds) * 1000).toInt()
        filterComplexBuilder.append(
            "[2:a]atrim=duration=$bowlEndDurationSeconds,asetpts=PTS-STARTPTS," +
                    "adelay=$bowlEndDelayMs|$bowlEndDelayMs[bowl_end_delayed]; "
        )
        filterComplexBuilder.append(
            "[aout][bowl_end_delayed]amix=" +
                    "inputs=2:" +
                    "duration=longest:" +
                    "normalize=0" +
                    "[aout]"
        )
        val filterComplex = filterComplexBuilder.toString()

        /* ───────────────────── Commande FFmpeg ───────────────────── */
        val cmd = buildString {
            append("-y ")
            append("-i \"$bowlStartPath\" ")
            append("-stream_loop -1 -i \"$musicPath\" ")
            append("-i \"$bowlEndPath\" ")
            introPath?.let { append("-i \"$it\" ") }

            for (i in 0 until potentialAffirmationCount) {
                append("-i \"${affirmationPaths[i % affirmationPaths.size]}\" ")
            }

            append("-filter_complex \"$filterComplex\" ")
            append("-t $selectedDurationInSeconds ")
            append("-map \"[aout]\" -c:a libmp3lame -b:a 192k ")
            append("\"$outputPath\"")
        }
        Log.d("MeditationPlay", "Executing FFmpeg command: $cmd")

        /* ───────────────────── Exécution ───────────────────── */
        FFmpegKit.executeAsync(cmd, { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                runOnUiThread {
                    //Toast.makeText(this, "Mixage réussi ! Fichier : $outputPath", Toast.LENGTH_SHORT).show()
                    findViewById<ImageView>(R.id.imageView4)        // ← nouvelle ligne
                        .setImageResource(R.drawable.logo_my_affirmation_tete_et_texte_vert)
                    hideOverlay()
                }
                callback(true)
            } else {
                runOnUiThread {
                    //Toast.makeText(this, "Échec du mixage.", Toast.LENGTH_SHORT).show()
                    Log.e("MeditationPlay", session.allLogsAsString)
                    hideOverlay()
                }
                callback(false)
            }
        }, { log ->
            Regex("time=(\\d+):(\\d+):(\\d+\\.\\d+)").find(log.message)?.let { m ->
                val (h, mnt, s) = m.destructured
                val cur = h.toInt() * 3600 + mnt.toInt() * 60 + s.toFloat()
                val pct = ((cur / selectedDurationInSeconds) * 100).toInt().coerceIn(0, 100)
                runOnUiThread {
                    circularProgressIndicator.progress = pct
                    circularProgressText.text = "$pct%"
                }
            }
        }, { /* statistics callback optionnel */ })
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

        /*if (finalAudioPath == null) {
            Toast.makeText(this, "Invalid main audio file path.", Toast.LENGTH_SHORT).show()
            return
        }
        val audioFile = File(finalAudioPath)
        if (!audioFile.exists()) {
            Toast.makeText(this, "Main audio file not found.", Toast.LENGTH_SHORT).show()
            return
        }*/

        val mediaItem = MediaItem.fromUri(Uri.fromFile(File(finalAudioPath)))

        Log.d("DBG", "playMainAudio called with $finalAudioPath")

        exoPlayer?.apply {
            setMediaItem(mediaItem)   // OK (thread UI)
            prepare()                 // déclenche BUFFERING → READY
            play()                    // isPlaying=true
        }

        startProgressTrackingWithCircularIndicator(durationTime)
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



    private fun resumeAllMediaPlayers() {
        mediaPlayer?.start()
    }

    private fun pauseAllMediaPlayers() {
        mediaPlayer?.pause()
    }

    private fun stopAllAudio() {
        exoPlayer?.run {
            stop()
            release()
        }
        exoPlayer = null
        mainHandler.removeCallbacksAndMessages(null)
        isTrackingProgress = false
        hideOverlay()
        //progressBar.visibility = View.GONE
        runOnUiThread {
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
