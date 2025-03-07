package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Step4 : Base() {  // Hérite de Base au lieu de AppCompatActivity

    private lateinit var btn_valider: Button
    private lateinit var btn_listenIntro: ImageButton

    private lateinit var numberPickerMinutes: NumberPicker
    private lateinit var numberPickerSeconds: NumberPicker
    private var selectedDurationInSeconds: Int = 0
    private var mediaPlayer1: MediaPlayer? = null
    private lateinit var introSwitch: Switch

    override fun getLayoutId(): Int {
        return R.layout.activity_step4  // Utilisez le layout adapté
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configuration des vues
        val textViewDuration: TextView = findViewById(R.id.textViewMinutesEdit)

        numberPickerMinutes = findViewById(R.id.numberPickerMinutes)
        numberPickerSeconds = findViewById(R.id.numberPickerSeconds)

        // Configurer les NumberPickers dans le code
        numberPickerMinutes.minValue = 0
        numberPickerMinutes.maxValue = 22  // Max 22 minutes

        numberPickerSeconds.minValue = 0
        numberPickerSeconds.maxValue = 59

        val updateDuration = {
            val minutes = numberPickerMinutes.value
            val seconds = numberPickerSeconds.value
            textViewDuration.text = "$minutes minutes $seconds secondes"
        }

        numberPickerMinutes.setOnValueChangedListener { _, _, _ -> updateDuration() }
        numberPickerSeconds.setOnValueChangedListener { _, _, _ -> updateDuration() }

        // Appel initial pour afficher la durée de départ
        updateDuration()

        // Récupération des extras de l'intent
        val filePaths = intent.getStringExtra("filePaths")
        val curentVoice = intent.getStringExtra("curentVoice")
        Log.d("test123", "onCreate: $curentVoice")

        val userTexts = intent.getStringArrayListExtra("userTexts")
        val userTextsSplit = intent.getStringArrayListExtra("userTextsSplit")
        val nom = intent.getStringExtra("nom")
        val curentAPIKey = intent.getStringExtra("curentAPIKey")
        val intention = intent.getBooleanExtra("intention", false)

        if (userTextsSplit != null) {
            for (text in userTextsSplit) {
                Log.d("test123456", "Received text: $text")
            }
        } else {
            Log.d("test123456", "No texts received")
        }

        // Gestion des insets pour le layout (si nécessaire)
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

        // Initialisation des vues
        btn_valider = findViewById<Button>(R.id.btn_Valid)
        btn_listenIntro = findViewById<ImageButton>(R.id.imageButtonStep4Listen)
        introSwitch = findViewById(R.id.intro)

        // Gestion de la visibilité en fonction de l'intention
        if (intention) {
            btn_listenIntro.visibility = View.GONE // Cache le TextView
            introSwitch.visibility = View.GONE  // Cache le Button
        } else {
            btn_listenIntro.visibility = View.VISIBLE // Affiche le TextView
            introSwitch.visibility = View.VISIBLE  // Affiche le Button
        }

        // Définir le listener pour le bouton Valider
        btn_valider.setOnClickListener {
            stopAudio()

            // Récupérer les valeurs des NumberPickers
            val minutes = numberPickerMinutes.value
            val seconds = numberPickerSeconds.value

            // Calculer la durée totale en secondes
            val totalDurationInSeconds = (minutes * 60) + seconds

            // Vérifier si la durée dépasse 22 minutes (1320 secondes)
            if (totalDurationInSeconds > 1320) {
                // Afficher un message d'erreur si la durée est trop longue
                Toast.makeText(this, "La durée ne doit pas dépasser 22 minutes.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener  // Empêche la suite de l'exécution si la durée est trop longue
            }

            selectedDurationInSeconds = totalDurationInSeconds
            val isIntroEnabled = introSwitch.isChecked

            // Passer directement les données à MeditationPlay
            if (filePaths != null && selectedDurationInSeconds > 30) {
                val intent = Intent(this, MeditationPlay::class.java).apply {
                    putExtra("filePaths", filePaths)
                    putExtra("selectedDuration", selectedDurationInSeconds)
                    putExtra("isIntroEnabled", isIntroEnabled)
                    putExtra("curentVoice", curentVoice)
                    putStringArrayListExtra("userTexts", userTexts)
                    putExtra("intention", intention)
                }
                startActivity(intent)
            } else {
                // Afficher un message d'erreur
                Toast.makeText(this, "Veuillez sélectionner une durée valide.", Toast.LENGTH_SHORT).show()
            }
        }

        // Définir le listener pour le bouton Écouter l'Intro
        btn_listenIntro.setOnClickListener {
            playAudioFromRaw(R.raw.intromeditation)
        }
    }

    // Fonctions existantes
    private fun generateTTSFilesForAllTexts(
        nom: String?,
        apikey: String?,
        userTextsSplit: ArrayList<String>?,
        userTexts: ArrayList<String>?,
        intention: Boolean
    ) {
        if (userTextsSplit != null) {
            for (index in 4..5) {
                if (index < userTextsSplit.size) {
                    val text = userTextsSplit[index]
                    if (nom != null && apikey != null) {
                        textToSpeech(text, nom, index, apikey, userTexts)
                    }
                }
            }
        }
    }

    private fun playAudioFromRaw(audioResId: Int) {
        if (mediaPlayer1 == null) {
            // Initialize and start playback
            mediaPlayer1 = MediaPlayer.create(this, audioResId)
            mediaPlayer1?.start()
            Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
        } else {
            if (mediaPlayer1?.isPlaying == true) {
                // Stop playback
                mediaPlayer1?.stop()
                mediaPlayer1?.reset()
                mediaPlayer1 = null
                Toast.makeText(this, "Stopping audio", Toast.LENGTH_SHORT).show()
            } else {
                // Restart playback
                mediaPlayer1 = MediaPlayer.create(this, audioResId)
                mediaPlayer1?.start()
                Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopAudio() {
        mediaPlayer1?.stop()
        mediaPlayer1?.release()
        mediaPlayer1 = null
    }

    private fun textToSpeech(
        text: String,
        nom: String,
        index: Int,
        voiceId: String,
        userTexts: ArrayList<String>?
    ) {
        val apiKey = "sk_1e85a97e6cdd33e449f8578f3fa7152594bdab061b0649b7" // Remplace avec ta clé API

        val client = OkHttpClient()
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()  // Créer le dossier si nécessaire
        }

        // Générer un fichier avec un nom unique basé sur l'index
        val generatedFilePath = "$basePath/voice_$index.mp3"
        userTexts?.add(generatedFilePath)

        val fullText = "moi $nom, $text."

        // Créer le corps de la requête en JSON
        val bodyJson = JSONObject().apply {
            put("text", fullText)
            put("model_id", "eleven_turbo_v2_5") // Use a multilingual model
            put("language_code", "fr") // Set language code to French
            put("voice_settings", JSONObject().apply {
                put("stability", 0.5)
                put("similarity_boost", 0.75)
                // You can adjust these values to fine-tune the accent
            })
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            bodyJson.toString()
        )

        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
            .addHeader("Content-Type", "application/json")
            .addHeader("xi-api-key", apiKey)
            .post(requestBody)
            .build()

        // Enqueue la requête pour qu'elle se fasse de manière asynchrone
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("testApi", "Erreur lors de l'appel API : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body

                if (responseBody != null) {
                    // Sauvegarder le fichier audio avec un nom unique basé sur l'index
                    val audioFileName = "voice_$index.mp3"
                    val audioFile = File(generatedFilePath)

                    try {
                        val outputStream = FileOutputStream(audioFile)
                        outputStream.write(responseBody.bytes()) // Écrire les octets dans le fichier
                        outputStream.close()

                        Log.d("testApi123", "Fichier audio sauvegardé à : ${audioFile.absolutePath}")

                        // Notification de succès
                        runOnUiThread {
                            Toast.makeText(this@Step4, "Fichier audio généré pour l'index $index", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: IOException) {
                        Log.e("testApi", "Erreur lors de la sauvegarde de l'audio : ${e.message}")
                    }

                } else {
                    Log.d("testApi", "Le corps de la réponse est null")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer1?.stop()
        mediaPlayer1?.release()
        mediaPlayer1 = null
    }
}