package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import java.io.File
import java.util.Locale
import kotlin.math.log
import android.util.Log
import android.widget.Toast

class step1FirstName : AppCompatActivity() {

    private lateinit var textToSpeechMale1: TextToSpeech
    private lateinit var textToSpeechMale2: TextToSpeech
    private lateinit var textToSpeechFemale1: TextToSpeech
    private lateinit var textToSpeechFemale2: TextToSpeech


    private lateinit var editText: EditText
    private lateinit var generateButton: Button
    private lateinit var test: Button
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var generatedFilePathMale1: String
    private lateinit var generatedFilePathMale2: String
    private lateinit var generatedFilePathFemale1: String
    private lateinit var generatedFilePathFemale2: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_step1_first_name)


        editText = findViewById(R.id.editTextTextStep1)
        generateButton = findViewById(R.id.buttonOkStep1)
        test= findViewById(R.id.buttonOkStep11)


        textToSpeechMale1 = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeechMale1.language = Locale.FRANCE
                textToSpeechMale1.setPitch(0.2f)
            }
        }

        textToSpeechMale2 = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeechMale2.language = Locale.FRANCE
                textToSpeechMale2.setPitch(0.4f) // Tonalité normale
            }
        }

        textToSpeechFemale1 = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeechFemale1.language = Locale.FRANCE
                textToSpeechFemale1.setPitch(1.2f) // Tonalité plus aiguë
            }
        }

        textToSpeechFemale2 = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeechFemale2.language = Locale.FRANCE
                textToSpeechFemale2.setPitch(1.0f) // Tonalité normale
            }
        }

        generateButton.setOnClickListener {
            val text = editText.text.toString()
            generateAudioFile(text)
        }

        test.setOnClickListener {
            if (this::generatedFilePathMale1.isInitialized) {
                playAudio(generatedFilePathMale2)
            } else {
                Toast.makeText(this, "Generate an audio file first", Toast.LENGTH_SHORT).show()
            }
        }

        val OkButton = findViewById<Button>(R.id.buttonOkStep1)
        // Définir les listeners pour les boutons
        OkButton.setOnClickListener {
            val text = editText.text.toString()

            // Créez une intention pour lancer Stage2
            val intent = Intent(this, Etape2Voix::class.java)
            intent.putExtra("nom", text)

            startActivity(intent)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun generateAudioFile(text: String) {
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        generatedFilePathMale1 = "$basePath/voiceMale1.mp3"
        val file = File(generatedFilePathMale1)
        textToSpeechMale1.synthesizeToFile(text, null, file, null)

        generatedFilePathMale2="$basePath/voiceMale2.mp3"
        val file1 = File(generatedFilePathMale2)
        textToSpeechMale2.synthesizeToFile(text, null, file1, null)

        generatedFilePathFemale1 = "$basePath/voiceFemale1.mp3"
        val file3 = File(generatedFilePathFemale1)
        textToSpeechFemale1.synthesizeToFile(text, null, file3, null)

        generatedFilePathFemale2="$basePath/voiceFemale2.mp3"
        val file4 = File(generatedFilePathFemale2)
        textToSpeechFemale2.synthesizeToFile(text, null, file4, null)

        Toast.makeText(this, "Audio file generated", Toast.LENGTH_SHORT).show()


    }



    private fun playAudio(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(file.path)
                prepare()
                start()
            }
            Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (this::textToSpeechFemale1.isInitialized) {
            textToSpeechFemale1.stop()
            textToSpeechFemale1.shutdown()
        }
        if (this::textToSpeechFemale2.isInitialized) {
            textToSpeechFemale2.stop()
            textToSpeechFemale2.shutdown()
        }
        if (this::textToSpeechMale1.isInitialized) {
            textToSpeechMale1.stop()
            textToSpeechMale1.shutdown()
        }
        if (this::textToSpeechMale2.isInitialized) {
            textToSpeechMale2.stop()
            textToSpeechMale2.shutdown()
        }

        super.onDestroy()
    }



}
