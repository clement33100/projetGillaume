package com.example.myapplicationv2

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MeditationPlay : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var preAudioPlayer: MediaPlayer? = null
    private var afterAudioPlayer: MediaPlayer? = null
    private lateinit var progressBar: ProgressBar
    private var handler: Handler? = null

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
        val sonboltibetain= "sonboltibetain.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathBolTibetain = copyRawResourceToInternalStorage(R.raw.sonboltibetain, sonboltibetain)

        val filePaths = intent.getStringExtra("filePaths")
        val selectedDurationInSeconds = intent.getIntExtra("selectedDuration", 0)


        if (isIntroEnabled) {
            playAudioFirst(savedFilePathIntromeditation,savedFilePathBolTibetain,filePaths,selectedDurationInSeconds)

        } else {

            playAudioSecond(savedFilePathBolTibetain,filePaths,selectedDurationInSeconds )

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


    private fun playAudioFirst(preAudioFilePath: String?,boltibetain :String?, mainAudioFilePath: String?, durationTime: Int) {

        // Initialize and start pre-audio playback
        preAudioPlayer = MediaPlayer().apply {
            setDataSource(preAudioFilePath)
            prepare()
            start()
        }
        Toast.makeText(this, "Playing pre-audio", Toast.LENGTH_SHORT).show()

        preAudioPlayer?.setOnCompletionListener {
            // After pre-audio completes, start the main audio
            playAudioSecond(boltibetain,mainAudioFilePath,durationTime )
        }

    }

    private fun playAudioSecond(preAudioFilePath: String?, mainAudioFilePath: String?, durationTime: Int) {

        // Initialize and start pre-audio playback
        preAudioPlayer = MediaPlayer().apply {
            setDataSource(preAudioFilePath)
            prepare()
            start()
        }
        Toast.makeText(this, "Playing pre-audio", Toast.LENGTH_SHORT).show()

        preAudioPlayer?.setOnCompletionListener {
            // After pre-audio completes, start the main audio
            playMainAudio(mainAudioFilePath,preAudioFilePath, durationTime)
        }



    }

    private fun playAudioLast(lastAudio: String?, durationTime: Int) {

        // Initialize and start pre-audio playback
        afterAudioPlayer = MediaPlayer().apply {
            setDataSource(lastAudio)
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
            }
            Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
        } else {
            if (mediaPlayer?.isPlaying == true ) {
                // Stop playback
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                Toast.makeText(this, "Stopping audio", Toast.LENGTH_SHORT).show()
            } else {
                // Switch to new audio file or restart current one
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(filePath)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
            }
        }



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

            mediaPlayer?.let {
                //handler = Handler(Looper.getMainLooper())
                handler?.postDelayed({
                    stopAudioEnd(boltibetain,durationTime)
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
                            stopAudioEnd(boltibetain,durationTime)
                        }, remainingTime * 1000L)

                    } else {
                        mediaPlayer?.start()  // Restart the song
                    }
                } else {

                    stopAudioEnd(boltibetain,durationTime)



                }
            }
        }



    }

    override fun onPause() {
        super.onPause()
        stopAudio()
    }
    private fun stopAudioEnd(boltibetain: String?,durationTime: Int) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler?.removeCallbacksAndMessages(null) // Remove any pending callbacks
        handler = null

        playAudioLast(boltibetain, durationTime)


    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler?.removeCallbacksAndMessages(null) // Remove any pending callbacks
        handler = null
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}