package com.example.myapplicationv2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType


class Step2 : Base() {  // Hérite de Base au lieu de AppCompatActivity
    private lateinit var container: LinearLayout
    private var textViewCount = 0
    private lateinit var buttonOk:Button
    private val userTexts = ArrayList<String>()  // Liste pour stocker les textes saisis
    private var currentIndex = 0 // Pour suivre l'élément en cours de traitement
    private lateinit var textToSpeech: TextToSpeech
    private val generateFiles = ArrayList<String>()  // Liste pour stocker les textes saisis


    override fun getLayoutId(): Int {
        return R.layout.activity_step2  // Retourne le layout spécifique à cette activité
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configuration du bouton de navigation dans Base, ne plus toucher à btn_burger ici


        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.FRANCE
                textToSpeech.setPitch(1.0f)
            }
        }


        buttonOk = findViewById(R.id.step2ok)
        container = findViewById(R.id.container)
        val addButton = findViewById<Button>(R.id.addButton)

        // Gestion des insets pour le layout
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val curentVoice = intent.getStringExtra("curentVoice")
        Log.i("test123456", "onCreate: "+curentVoice.toString())
        val nom = intent.getStringExtra("nom")


        buttonOk.setOnClickListener{
            generateTTSFilesForAllTexts(nom)

            //textToSpeech(userTexts.get(0),"VR6AewLTigWG4xSOukaG")

            if(curentVoice!=null){



                val intent = Intent(this, step3Music::class.java)
                intent.putExtra("curentVoice", curentVoice)
                intent.putStringArrayListExtra("userTexts", generateFiles)
                startActivity(intent)

            }else{
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()

            }
        }


        addButton.setOnClickListener {
            Log.d("Step2", "Button clicked")
            showAddTextDialog()
        }
    }



    private fun generateTTSFilesForAllTexts(nom :String?) {
        for ((index, text) in userTexts.withIndex()) {
            //generateAudioFileForText(text, index)
            if(nom!=null) textToSpeech(text,nom, index,"VR6AewLTigWG4xSOukaG")
        }
    }

    // Générer un fichier audio pour un texte donné avec un nom unique
    private fun generateAudioFileForText(text: String, index: Int) {
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()  // Créer le dossier si nécessaire
        }

        // Générer un fichier avec un nom unique basé sur l'index
        val generatedFilePath = "$basePath/voice_$index.mp3"
        val file = File(generatedFilePath)
        generateFiles.add(generatedFilePath)
        Log.i("test12345", "generateAudioFileForText: "+generatedFilePath)
        // Utiliser TextToSpeech pour synthétiser le fichier
        textToSpeech.synthesizeToFile(text, null, file, null)

        // Notification utilisateur
        Toast.makeText(this, "Fichier audio généré pour le texte $index", Toast.LENGTH_SHORT).show()
    }


    private fun showAddTextDialog() {
        Log.d("Step2", "Showing dialog")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ajouter un texte")

        // Set up the input
        val input = EditText(this)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Valider") { dialog, which ->
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                userTexts.add(text)  // Ajouter le texte à la liste
                addTextView(text)
            }
        }
        builder.setNegativeButton("Annuler") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun addTextView(text: String) {
        textViewCount++
        val numberedText = "Affirmation $textViewCount : $text"
        Log.d("Step2", "Adding TextView with text: $numberedText")

        // Create a new horizontal LinearLayout
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.HORIZONTAL

        // Create the TextView
        val textView = TextView(this)
        textView.text = numberedText
        textView.textSize = 18f
        textView.setPadding(8, 30, 8, 0)

        // Create the delete ImageButton
        val deleteButton = ImageButton(this)
        deleteButton.setImageResource(R.drawable.imgbtn_delete)
        deleteButton.setBackgroundColor(0x00000000)  // Make background transparent
        deleteButton.setOnClickListener {
            container.removeView(linearLayout)
            userTexts.remove(text)  // Supprimer le texte de la liste
        }

        // Add TextView and ImageButton to the horizontal LinearLayout
        linearLayout.addView(textView)
        linearLayout.addView(deleteButton)

        // Add the horizontal LinearLayout to the container
        container.addView(linearLayout)
    }

    private fun textToSpeech(text: String,nom:String, index: Int, voiceId: String) {
        val apiKey = "sk_1e85a97e6cdd33e449f8578f3fa7152594bdab061b0649b7" // Remplace avec ta clé API

        val client = OkHttpClient()
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()  // Créer le dossier si nécessaire
        }

        // Générer un fichier avec un nom unique basé sur l'index
        val generatedFilePath = "$basePath/voice_$index.mp3"
        //val file = File(generatedFilePath)
        generateFiles.add(generatedFilePath)

        val fullText = "$nom,tu es $text"

        // Créer le corps de la requête en JSON
        val bodyJson = JSONObject().apply {
            put("text", fullText)
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
                    // Sauvegarder le fichier audio avec un nom unique basé sur l'index




                    val audioFileName = "voice_$index.mp3"
                    val audioFile = File(generatedFilePath)

                    try {
                        val outputStream = FileOutputStream(audioFile)
                        outputStream.write(responseBody.bytes()) // Écrire les octets dans le fichier
                        outputStream.close()

                        Log.d("testApi123", "Fichier audio sauvegardé à : ${audioFile.absolutePath}")

                        // Ajouter le chemin du fichier généré à la liste `generateFiles`

                        // Notification de succès
                        runOnUiThread {
                            Toast.makeText(this@Step2, "Fichier audio généré pour l'index $index", Toast.LENGTH_SHORT).show()
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

    /*private  fun textToSpeech(text: String,voiceId: String) {
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
                      //playAudio(audioFile)

                    } catch (e: IOException) {
                        Log.e("testApi", "Erreur lors de la sauvegarde de l'audio : ${e.message}")
                    }

                } else {
                    Log.d("testApi", "Le corps de la réponse est null")
                }
            }
        })
    }*/



}
