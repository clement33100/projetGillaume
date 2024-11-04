package com.example.myapplicationv2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType

class Step2 : Base() {
    private lateinit var container: LinearLayout
    private var textViewCount = 0
    private lateinit var buttonOk: Button
    private val userTexts = ArrayList<String>()
    private var currentIndex = 0
    private lateinit var textToSpeech: TextToSpeech
    private val generateFiles = ArrayList<String>()

    override fun getLayoutId(): Int {
        return R.layout.activity_step2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        Log.i("Step2", "onCreate: $curentVoice")
        val nom = intent.getStringExtra("nom")
        val curentAPIKey = intent.getStringExtra("curentAPIKey")

        // **Ajouter les affirmations par défaut**
        addDefaultAffirmations()

        buttonOk.setOnClickListener {
            generateTTSFilesForAllTexts(nom, curentAPIKey)

            if (curentVoice != null) {
                val intent = Intent(this, step3Music::class.java)
                intent.putExtra("curentVoice", curentVoice)
                intent.putStringArrayListExtra("userTexts", generateFiles)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }

        addButton.setOnClickListener {
            Log.d("Step2", "Button clicked")
            showAddTextDialog()
        }
    }

    private fun addDefaultAffirmations() {
        // Vos affirmations par défaut
        val defaultAffirmations = listOf(
            "Je suis confiant(e) et capable.",
            "Chaque jour est une nouvelle opportunité."
        )

        for (affirmation in defaultAffirmations) {
            userTexts.add(affirmation)
            addTextView(affirmation)
        }
    }

    private fun generateTTSFilesForAllTexts(nom: String?, apikey: String?) {
        for ((index, text) in userTexts.withIndex()) {
            if (nom != null) apikey?.let { textToSpeechAPI(text, nom, index, it) }
        }
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
        textView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        // Create the delete ImageButton
        val deleteButton = ImageButton(this)
        deleteButton.setImageResource(R.drawable.imgbtn_delete)
        deleteButton.setBackgroundColor(0x00000000)  // Make background transparent
        deleteButton.setOnClickListener {
            container.removeView(linearLayout)
            userTexts.remove(text)  // Supprimer le texte de la liste
            textViewCount--
            updateTextNumbers()
        }

        // Add TextView and ImageButton to the horizontal LinearLayout
        linearLayout.addView(textView)
        linearLayout.addView(deleteButton)

        // Add the horizontal LinearLayout to the container
        container.addView(linearLayout)
    }

    private fun updateTextNumbers() {
        var count = 1
        for (i in 0 until container.childCount) {
            val linearLayout = container.getChildAt(i) as LinearLayout
            val textView = linearLayout.getChildAt(0) as TextView
            val text = userTexts[i]
            textView.text = "Affirmation $count : $text"
            count++
        }
    }

    private fun textToSpeechAPI(text: String, nom: String, index: Int, voiceId: String) {
        val apiKey = "VOTRE_CLE_API"  // Remplacez par votre clé API

        val client = OkHttpClient()
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()  // Créer le dossier si nécessaire
        }

        // Générer un fichier avec un nom unique basé sur l'index
        val generatedFilePath = "$basePath/voice_$index.mp3"
        generateFiles.add(generatedFilePath)

        val fullText = "Moi $nom, $text"

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
                Log.e("Step2", "Erreur lors de l'appel API : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body

                if (responseBody != null) {
                    val audioFile = File(generatedFilePath)

                    try {
                        val outputStream = FileOutputStream(audioFile)
                        outputStream.write(responseBody.bytes()) // Écrire les octets dans le fichier
                        outputStream.close()

                        Log.d("Step2", "Fichier audio sauvegardé à : ${audioFile.absolutePath}")

                        // Notification de succès
                        runOnUiThread {
                            Toast.makeText(
                                this@Step2,
                                "Fichier audio généré pour l'index $index",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: IOException) {
                        Log.e("Step2", "Erreur lors de la sauvegarde de l'audio : ${e.message}")
                    }

                } else {
                    Log.d("Step2", "Le corps de la réponse est null")
                }
            }
        })
    }
}
