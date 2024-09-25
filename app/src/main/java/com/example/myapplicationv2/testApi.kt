package com.example.myapplicationv2

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class testApi : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test_api)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Trouver le bouton par son ID
        val buttonTTS: Button = findViewById(R.id.buttonTTS)

        // Ajouter un listener pour le clic du bouton
        buttonTTS.setOnClickListener {
            Log.d("testApi", "Bouton cliqué, démarrage de l'appel API...")

            // Appeler l'API dans une coroutine
            CoroutineScope(Dispatchers.IO).launch {
                textToSpeech("Ton texte à convertir ici")
            }
        }
    }

    // Fonction suspendue pour l'appel API avec OkHttp
    private suspend fun textToSpeech(text: String) {
        val voiceId = "VR6AewLTigWG4xSOukaG" // Remplace avec ton voice_id
        val apiKey = "sk_1e85a97e6cdd33e449f8578f3fa7152594bdab061b0649b7" // Remplace avec ta clé API

        val client = OkHttpClient()

        // Créer le corps de la requête en JSON
        val bodyJson = JSONObject().apply {
            put("text", text)
            put("voice_settings", JSONObject().apply {
                put("stability", 0.5)
                put("similarity_boost", 0.75)
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
                    // Sauvegarder le fichier audio
                    val audioFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "output_audio.mp3")

                    try {
                        val outputStream = FileOutputStream(audioFile)
                        outputStream.write(responseBody.bytes()) // Écrire les octets dans le fichier
                        outputStream.close()

                        Log.d("testApi", "Fichier audio sauvegardé à : ${audioFile.absolutePath}")

                        // Lecture de l'audio après avoir sauvegardé le fichier
                        playAudio(audioFile)

                    } catch (e: IOException) {
                        Log.e("testApi", "Erreur lors de la sauvegarde de l'audio : ${e.message}")
                    }

                } else {
                    Log.d("testApi", "Le corps de la réponse est null")
                }
            }
        })
    }

    // Fonction pour jouer l'audio
    private fun playAudio(audioFile: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath) // Spécifie le fichier à lire
                prepare() // Prépare la lecture
                start() // Démarre la lecture
            }
            Log.d("testApi", "Lecture de l'audio en cours...")

            mediaPlayer?.setOnCompletionListener {
                Log.d("testApi", "Lecture de l'audio terminée.")
                it.release() // Libère le MediaPlayer une fois l'audio terminé
            }
        } catch (e: IOException) {
            Log.e("testApi", "Erreur lors de la lecture de l'audio : ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Libérer les ressources du MediaPlayer si elles sont encore en cours d'utilisation
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
