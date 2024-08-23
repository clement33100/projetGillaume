package com.example.myapplicationv2

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MeditationPlay : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var remainingTime: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.meditation_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val filePaths = intent.getStringExtra("filePaths")
        Log.i("test1234567", "onCreate: "+filePaths)

        val selectedDurationInSeconds = intent.getIntExtra("selectedDuration", 0)

        Log.i("test123456", "onCreate: "+selectedDurationInSeconds.toString())


        playAudio(filePaths.toString(),selectedDurationInSeconds)


    }




    private fun playAudio(filePath: String,durationTime: Int) {

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
        val songDuration = mediaPlayer?.duration?.div(1000) ?: 0

        var remainingTime = durationTime


        if (durationTime < songDuration) {
            // If the chosen duration is less than the song's duration, stop the audio after the chosen time
            mediaPlayer?.setOnCompletionListener(null)  // Disable looping
            mediaPlayer?.seekTo(0)  // Reset to the beginning
            mediaPlayer?.start()

            mediaPlayer?.let {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
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
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            stopAudio()
                        }, remainingTime * 1000L)
                    } else {
                        mediaPlayer?.start()  // Restart the song
                    }
                } else {
                    stopAudio()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopAudio()
    }
    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}