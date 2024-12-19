package com.example.myapplicationv2

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
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
import java.util.Locale

class MeditationPlay : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var preAudioPlayer: MediaPlayer? = null
    private var afterAudioPlayer: MediaPlayer? = null
    private var affirmationAudioPlayer: MediaPlayer? = null
    private var TTSAudioPlayer: MediaPlayer? = null


    private var EndAudioPlayer: MediaPlayer? = null

    private lateinit var progressBar: ProgressBar
    private var handler: Handler? = null
    private var handlerAffirmation: Handler? = null
    private var isPaused = false
    private var currentIndex = 0 // Ajout d'un index pour suivre l'élément actuel de userTexts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.meditation_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intromeditation = "intromeditation.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathIntromeditation = copyRawResourceToInternalStorage(R.raw.intromeditation, intromeditation)
        val isIntroEnabled = intent.getBooleanExtra("isIntroEnabled", false)


        val boltibetain= "boltibetainson.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathBolTibetain = copyRawResourceToInternalStorage(R.raw.boltibetainson, boltibetain)




        val filePaths = intent.getStringExtra("filePaths")
        val selectedDurationInSeconds = intent.getIntExtra("selectedDuration", 0)
        val curentVoice = intent.getStringExtra("curentVoice")// prenom de la personne

        val userTexts = intent.getStringArrayListExtra("userTexts")?.distinct()?.toCollection(ArrayList())
        if (userTexts != null) {
            for (text in userTexts) {
                Log.d("test1234888", "Received text: $text")
            }
        } else {
            Log.d("test1234888", "No texts received")
        }




        if (isIntroEnabled) {
            playAudioFirst(savedFilePathIntromeditation,savedFilePathBolTibetain,curentVoice,userTexts)
            playMainAudio(filePaths,savedFilePathBolTibetain,selectedDurationInSeconds)


        } else {
            /*if (userTexts != null) {
                playAudioTTSVoice(userTexts.get(0))
            }*/

            playAudioSecond(savedFilePathBolTibetain,curentVoice,userTexts)
            playMainAudio(filePaths,savedFilePathBolTibetain,selectedDurationInSeconds)

        }
        val pauseButton = findViewById<ImageButton>(R.id.imageButtonPause)

        pauseButton.setOnClickListener {
            if (isPaused) {
                // Relancer la lecture de tous les players
                mediaPlayer?.start()
                affirmationAudioPlayer?.start()
                afterAudioPlayer?.start()
                preAudioPlayer?.start()
                EndAudioPlayer?.start()
                TTSAudioPlayer?.start()

                pauseButton.setImageResource(R.drawable.imgunpause)
                isPaused = false
            } else {
                // Mettre en pause tous les players
                mediaPlayer?.pause()
                affirmationAudioPlayer?.pause()
                afterAudioPlayer?.pause()
                preAudioPlayer?.pause()
                EndAudioPlayer?.pause()
                TTSAudioPlayer?.pause()
                pauseButton.setImageResource(R.drawable.imgpause)
                isPaused = true
            }
        }
    }


    private fun copyRawResourceToInternalStorage(resourceId: Int, fileName: String): String? {
        val uri = Uri.parse("android.resource://${packageName}/$resourceId")
        return saveFileToInternalStorage(uri, fileName)
    }
    private fun saveFileToInternalStorage(uri: Uri, fileName: String): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // 4KB buffer
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }

        return file.absolutePath
    }


    private fun playAudioFirst(boltibetain: String?,preAudioFilePath :String?,voice :String?,userTexts: ArrayList<String>?) {

        // Initialize and start pre-audio playback
        preAudioPlayer = MediaPlayer().apply {
            setDataSource(boltibetain)
            prepare()
            start()
        }

        preAudioPlayer?.setOnCompletionListener {
            // After pre-audio completes, start the main audio
            playIntro(preAudioFilePath,voice,userTexts)

        }


    }

    private fun playIntro(preAudioFilePath: String?,voice :String?,userTexts: ArrayList<String>?) {

        // Initialize and start pre-audio playback
        afterAudioPlayer = MediaPlayer().apply {
            setDataSource(preAudioFilePath)
            prepare()
            start()
        }

        afterAudioPlayer?.setOnCompletionListener {
            Handler(Looper.getMainLooper()).postDelayed({
                // Après 1 seconde, démarrer la lecture de l'affirmation principale
                if(userTexts!=null)
                startReadingAffirmations(userTexts)
            }, 3000) // Délai de 1 seconde
        }

    }
    private fun playAffirmation(voice :String?) {

        // Initialize and start pre-audio playback
        affirmationAudioPlayer = MediaPlayer().apply {
            setDataSource(voice)
            prepare()
            start()
            affirmationAudioPlayer?.setVolume(1.0f, 1.0f)
        }

    }

    private fun startReadingAffirmations(userTexts: ArrayList<String>) {
        handlerAffirmation = Handler(Looper.getMainLooper())

        // Créer un Runnable pour lire chaque texte avec un délai
        val affirmationRunnable = object : Runnable {
            override fun run() {
                if (userTexts.isNotEmpty()) {
                    // Lire l'affirmation actuelle
                    playAffirmation(userTexts[currentIndex])

                    // Passer à l'affirmation suivante
                    currentIndex++

                    // Si on arrive à la fin de la liste, recommencer depuis le début
                    if (currentIndex >= userTexts.size) {
                        currentIndex = 0
                    }

                    // Relancer le handler après 5 secondes
                    handlerAffirmation?.postDelayed(this, 11000)  // Délai de 5 secondes
                }
            }
        }

        // Démarrer la première lecture
        handlerAffirmation?.post(affirmationRunnable)
    }



    private fun playAudioSecond(preAudioFilePath: String?,voice: String?,userTexts: ArrayList<String>?) {

        // Initialize and start pre-audio playback
        preAudioPlayer = MediaPlayer().apply {
            setDataSource(preAudioFilePath)
            prepare()
            start()
        }

        preAudioPlayer?.setOnCompletionListener {
            // After pre-audio completes, start the main audio
            if(userTexts!=null)
            startReadingAffirmations(userTexts)
        }

    }

    private fun playAudioThird(preAudioFilePath: String?) {

        // Initialize and start pre-audio playback
        EndAudioPlayer = MediaPlayer().apply {
            setDataSource(preAudioFilePath)
            prepare()
            start()
        }

    }

    private fun playAudioTTSVoice(preAudioFilePath: String?) {

        // Initialize and start pre-audio playback
        TTSAudioPlayer = MediaPlayer().apply {
            setDataSource(preAudioFilePath)
            prepare()
            start()
        }

    }

    private fun playMainAudio(filePath: String?,boltibetain: String?,durationTime: Int) {
        val initialDuration = durationTime
        var progressBarTimer = durationTime
        var remainingTime = durationTime

        if (mediaPlayer == null) {

            // Initialize and start playback
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setVolume(0.1778f, 0.1778f)
            }
        } else {
            if (mediaPlayer?.isPlaying == true ) {
                // Stop playback
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer?.setVolume(0.1778f, 0.1778f)

            } else {
                // Switch to new audio file or restart current one
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(filePath)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                mediaPlayer?.setVolume(0.1778f, 0.1778f)

            }
        }


        fadeInAudio()
        progressBar = findViewById(R.id.progressBar)
        progressBar.max = durationTime
        progressBar.progress = 0


        val songDuration = mediaPlayer?.duration?.div(1000) ?: 0
        handler = Handler(Looper.getMainLooper())

        handler?.post(object : Runnable {
            override fun run() {
                if (progressBarTimer > 0) {
                    progressBarTimer--
                    progressBar.progress = initialDuration - progressBarTimer
                    handler?.postDelayed(this, 1000) // Update every second
                    if (durationTime < songDuration) {
                        // If the chosen duration is less than the song's duration, stop the audio after the chosen time
                    } else if (progressBarTimer < songDuration) {
                        // If the remaining time is less than the song's duration, stop the song when the timer reaches 0
                        handler?.postDelayed({
                        }, progressBarTimer * 1000L)
                    }
                    if (progressBarTimer == 12) {  // Example: start fade-out 5 seconds before end
                        fadeOutAudio()
                        stopAudioAffirmation()
                    }
                    if(progressBarTimer ==10){
                        playAudioThird(boltibetain)

                    }

                } else {
                    progressBar.progress = initialDuration
                }
            }
        })



        if (durationTime < songDuration) {
            // If the chosen duration is less than the song's duration, stop the audio after the chosen time
            mediaPlayer?.setOnCompletionListener(null)  // Disable looping
            mediaPlayer?.seekTo(0)  // Reset to the beginning
            mediaPlayer?.start()
            mediaPlayer?.setVolume(0.1778f, 0.1778f)

            mediaPlayer?.let {
                //handler = Handler(Looper.getMainLooper())
                handler?.postDelayed({
                    //stopAudioEnd(boltibetain,durationTime)
                    stopAudio()
                }, durationTime * 1000L)  // Stop after the chosen time in milliseconds
            }
        } else {
            // If the song is shorter than the chosen duration, loop the song until the chosen time is over
            remainingTime -= songDuration

            mediaPlayer?.setOnCompletionListener {

                if (remainingTime > 0) {
                    if (remainingTime < songDuration) {
                        mediaPlayer?.start()
                        // Stop the song when the timer reaches 0
                        //val handler = Handler(Looper.getMainLooper())
                        handler?.postDelayed({
                           // stopAudioEnd(boltibetain,durationTime)
                            stopAudio()
                        }, remainingTime * 1000L)



                    } else {
                        mediaPlayer?.start()  // Restart the song
                    }
                } else {

                   // stopAudioEnd(boltibetain,durationTime)
                    stopAudio()


                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopAudio()
    }
    private fun stopAudioAffirmation(){
        affirmationAudioPlayer?.stop()
        affirmationAudioPlayer?.release()
        affirmationAudioPlayer = null

        handlerAffirmation?.removeCallbacksAndMessages(null) // Remove any pending callbacks
        handlerAffirmation = null
    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        preAudioPlayer?.stop()
        preAudioPlayer?.release()
        preAudioPlayer = null
        afterAudioPlayer?.stop()
        afterAudioPlayer?.release()
        afterAudioPlayer = null
        EndAudioPlayer?.stop()
        EndAudioPlayer?.release()
        EndAudioPlayer = null
        affirmationAudioPlayer?.stop()
        affirmationAudioPlayer?.release()
        affirmationAudioPlayer = null
        handlerAffirmation?.removeCallbacksAndMessages(null) // Remove any pending callbacks
        handlerAffirmation = null
        handler?.removeCallbacksAndMessages(null) // Remove any pending callbacks
        handler = null
    }

    private fun fadeInAudio() {
        val fadeDuration = 20000L // 5 seconds fade-in duration
        val fadeStep = 50L  // Step time in milliseconds
        val maxVolume = 0.5f
        val increment = maxVolume / (fadeDuration / fadeStep)

        var volume = 0f

        handler = Handler(Looper.getMainLooper())
        handler?.post(object : Runnable {
            override fun run() {
                if (volume < maxVolume) {
                    volume += increment
                    mediaPlayer?.setVolume(volume, volume)
                    handler?.postDelayed(this, fadeStep)
                } else {
                    mediaPlayer?.setVolume(maxVolume, maxVolume) // Ensure full volume is set
                }
            }
        })
    }

    private fun fadeOutAudio() {
        val fadeDuration = 23000L // 30 seconds fade-out duration
        val fadeStep = 50L  // Step time in milliseconds
        val maxVolume = 0.5f

        // Réduire le décrément pour rendre la baisse du volume plus douce
        val decrement = maxVolume / (fadeDuration / fadeStep)  // Diviser le décrément par 2 pour le rendre moins élevé

        var volume = maxVolume

        handler = Handler(Looper.getMainLooper())
        handler?.post(object : Runnable {
            override fun run() {
                if (volume > 0f) {
                    volume -= decrement
                    if (volume < 0f) volume = 0f // S'assurer que le volume ne descend pas en dessous de 0
                    mediaPlayer?.setVolume(volume, volume)
                    handler?.postDelayed(this, fadeStep)
                } else {
                    mediaPlayer?.setVolume(0f, 0f)  // Ensure volume is set to 0
                    stopAudio()
                }
            }
        })
    }




    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        preAudioPlayer?.release()
        preAudioPlayer = null
        afterAudioPlayer?.release()
        afterAudioPlayer = null
        EndAudioPlayer?.release()
        EndAudioPlayer = null
        affirmationAudioPlayer?.release()
        affirmationAudioPlayer = null
    }

}