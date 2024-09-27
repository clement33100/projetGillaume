package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextClock
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.util.Locale

class Step4 : AppCompatActivity() {

    private lateinit var btn_valider: Button
    private lateinit var btn_listenIntro: ImageButton

    private lateinit var numberPickerMinutes: NumberPicker
    private lateinit var numberPickerSeconds: NumberPicker
    private lateinit var numberPickerHours: NumberPicker
    private var selectedDurationInSeconds: Int = 0
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var introSwitch: Switch


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_step4)

        numberPickerMinutes = findViewById(R.id.numberPickerMinutes)
        numberPickerSeconds = findViewById(R.id.numberPickerSeconds)
        numberPickerHours = findViewById(R.id.numberPickerHours)
        // Configurer les NumberPickers dans le code
        numberPickerMinutes.minValue = 0
        numberPickerMinutes.maxValue = 59

        numberPickerSeconds.minValue = 0
        numberPickerSeconds.maxValue = 59

        numberPickerHours.minValue=0
        numberPickerHours.maxValue=23





        val filePaths = intent.getStringExtra("filePaths")
        val curentVoice = intent.getStringExtra("curentVoice")
        Log.d("test123", "onCreate: "+curentVoice.toString())

        val userTexts = intent.getStringArrayListExtra("userTexts")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_valider=findViewById<Button>(R.id.btn_Valid)
        btn_listenIntro=findViewById<ImageButton>(R.id.imageButtonStep4Listen)
        introSwitch = findViewById(R.id.intro)

        btn_valider.setOnClickListener {
            val hours = numberPickerHours.value
            val minutes = numberPickerMinutes.value
            val seconds = numberPickerSeconds.value
            selectedDurationInSeconds = hours * 3600 + minutes * 60 + seconds
            val isIntroEnabled = introSwitch.isChecked

            // Passer directement les données à Step5
            if (filePaths != null && selectedDurationInSeconds > 30  ) {
                // Passer les données à l'activité suivante
                    val intent = Intent(this, MeditationPlay::class.java).apply {
                    putExtra("filePaths", filePaths)

                    putExtra("selectedDuration", selectedDurationInSeconds)
                    putExtra("isIntroEnabled", isIntroEnabled)
                    putExtra("curentVoice", curentVoice)
                    putStringArrayListExtra("userTexts", userTexts)

                    }
                startActivity(intent)
            } else {
                // Afficher un message d'erreur ou gérer le cas où filePaths est null
                Toast.makeText(this, "Please select a valid time.", Toast.LENGTH_SHORT).show()            }
        }

        btn_listenIntro.setOnClickListener{


            playAudioFromRaw(R.raw.intromeditation)

        }


    }



    private fun playAudioFromRaw(audioResId: Int) {
        if (mediaPlayer == null) {
            // Initialize and start playback
            mediaPlayer = MediaPlayer.create(this, audioResId)
            mediaPlayer?.start()
            Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
        } else {
            if (mediaPlayer?.isPlaying == true) {
                // Stop playback
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer = null
                Toast.makeText(this, "Stopping audio", Toast.LENGTH_SHORT).show()
            } else {
                // Restart playback
                mediaPlayer = MediaPlayer.create(this, audioResId)
                mediaPlayer?.start()
                Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

}