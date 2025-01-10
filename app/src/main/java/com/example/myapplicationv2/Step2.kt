package com.example.myapplicationv2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class Step2 : Base() {
    private lateinit var container: LinearLayout
    private var textViewCount = 0
    private lateinit var buttonOk: Button
    private val userTexts = ArrayList<String>()  // Liste pour stocker les textes saisis
    private lateinit var textToSpeech: TextToSpeech
    private val generateFiles = ArrayList<String>()  // Liste pour stocker les chemins des fichiers générés
    private lateinit var titleStep2: TextView
    override fun getLayoutId(): Int {
        return R.layout.activity_step2  // Retourne le layout spécifique à cette activité
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

        // Fonctions de formatage du texte
        fun formatText(text: String, sizeMultiplier: Float, isBold: Boolean = false): SpannableString {
            val spannable = SpannableString(text)
            spannable.setSpan(RelativeSizeSpan(sizeMultiplier), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (isBold) {
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return spannable
        }

        fun formatHtmlText(htmlText: String, sizeMultiplier: Float = 1.0f): SpannableStringBuilder {
            val spannable = SpannableStringBuilder(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY))
            spannable.setSpan(
                RelativeSizeSpan(sizeMultiplier),
                0,
                spannable.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }

        val btnShowAdvices = findViewById<Button>(R.id.btn_show_advices)
        val scrollView = findViewById<ScrollView>(R.id.scrollViewAffirm)
        var isAdviceExpanded = false
        btnShowAdvices.isAllCaps = false

        // Préparation du texte de conseils
        var title = formatHtmlText("<b>CONSEILS PRATIQUES</b>", 1f)
        val advice1 = formatHtmlText("<b>FORMULE AU PRÉSENT</b> comme si c’était une réalité. <i>\"Je suis confiant.\"</i>", 0.8f)
        val advice2 = formatHtmlText("<b>SOIS POSITIF</b> en te concentrant sur ce que tu veux, pas sur ce que tu veux éviter", 0.8f)
        val advice3 = formatHtmlText("<b>CHOISIS TES MOTS</b> riches de sens pour toi", 0.8f)
        val advice4 = formatHtmlText("<b>SOIS CLAIR</b>, simple et précis", 0.8f)
        val advice5 = formatHtmlText("<b>RENFORCE TES AFFIRMATIONS</b> avec des exemples concrets de ta vie qui les confirment : <i>\"Je suis confiant parce que j’ai déjà atteint 'cet objectif' que je m’étais fixé.\"</i>", 0.8f)
        val advice6 = formatHtmlText("<b>SURMONTE TES RÉSISTANCES</b> avec : « Je m’ouvre à la possibilité de... ».", 0.8f)
        val advice7 = formatHtmlText("<b>ÉVOLUE EN DOUCEUR</b> en ajoutant : « À mon juste rythme, en douceur. »", 0.8f)


        val arrowUpDrawable = ContextCompat.getDrawable(this, R.drawable.arrowbottomtom)
        val insetArrow = InsetDrawable(arrowUpDrawable, 0, -28, 0, 0)
        insetArrow?.setBounds(0, 0, insetArrow.intrinsicWidth, insetArrow.intrinsicHeight)

        val arrowDownDrawable = ContextCompat.getDrawable(this, R.drawable.arrowdown)
        arrowDownDrawable?.setBounds(0, 0, arrowDownDrawable.intrinsicWidth, arrowDownDrawable.intrinsicHeight)

        btnShowAdvices.setOnClickListener {
            if (!isAdviceExpanded) {
                btnShowAdvices.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                val finalTextBuilder = SpannableStringBuilder()
                finalTextBuilder.append(title)

                if (arrowUpDrawable != null) {
                    finalTextBuilder.append("         ")
                    finalTextBuilder.setSpan(
                        ImageSpan(insetArrow, ImageSpan.ALIGN_BASELINE),
                        finalTextBuilder.length - 1,
                        finalTextBuilder.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                finalTextBuilder.append("\n\n")
                finalTextBuilder.append(advice1).append("\n\n")
                finalTextBuilder.append(advice2).append("\n\n")
                finalTextBuilder.append(advice3).append("\n\n")
                finalTextBuilder.append(advice4).append("\n\n")
                finalTextBuilder.append(advice5).append("\n\n")
                finalTextBuilder.append(advice6).append("\n\n")
                finalTextBuilder.append(advice7)

                btnShowAdvices.text = finalTextBuilder

                btnShowAdvices.post {
                    scrollView.post {
                        scrollView.smoothScrollTo(0, btnShowAdvices.bottom)
                    }
                }

            } else {
                val minimalTextBuilder = SpannableStringBuilder()
                minimalTextBuilder.append(title)
                btnShowAdvices.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowdown, 0)
                btnShowAdvices.text = minimalTextBuilder

                scrollView.post {
                    scrollView.smoothScrollTo(0, scrollView.top)
                }
            }

            btnShowAdvices.gravity = Gravity.CENTER_VERTICAL
            isAdviceExpanded = !isAdviceExpanded
        }

        buttonOk = findViewById(R.id.step2ok)
        container = findViewById(R.id.container)
        val addButton = findViewById<Button>(R.id.addButton)

        // Gestion des insets
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val curentVoice = intent.getStringExtra("curentVoice")
        val nom = intent.getStringExtra("nom")
        val curentAPIKey = intent.getStringExtra("curentAPIKey")
        val intention = intent.getBooleanExtra("intention", false)

        titleStep2 = findViewById(R.id.titlestep2) // Assurez-vous que `R.id.title` est défini dans votre layout

        if(intention){
            titleStep2.setText("ÉCRIS TES INTENTIONS")
            btnShowAdvices.visibility = View.GONE

            val predefinedTexts = listOf("Intention 1", "Intention 2")
            predefinedTexts.forEach {
                userTexts.add(it) // on ajoute le texte à userTexts
                addTextView(it, userTexts,intention) // on affiche dans le layout
            }



        }else{
            val predefinedTexts = listOf("Je \"affirmation 1\"", "Je \"affirmation 2\"")
            predefinedTexts.forEach {
                userTexts.add(it) // on ajoute le texte à userTexts
                addTextView(it, userTexts,intention) // on affiche dans le layout
            }


        }


        addButton.setOnClickListener {
            // Ajout d'une nouvelle affirmation vide
            val newText = ""
            userTexts.add(newText)
            addTextView(newText, userTexts,intention)
        }



        buttonOk.setOnClickListener {
            // userTexts est déjà mis à jour grâce aux TextWatcher
            generateTTSFilesForAllTexts(nom, curentAPIKey,intention)

            if (curentVoice != null) {
                val intent = Intent(this, step3Music::class.java)
                intent.putExtra("curentVoice", curentVoice)
                intent.putStringArrayListExtra("userTexts", generateFiles)
                intent.putExtra("intention", intention)

                if (userTexts.size > 2) {
                    intent.putExtra("curentAPIKey", curentAPIKey)
                    intent.putExtra("nom", nom)
                    intent.putStringArrayListExtra("userTextsSplit", userTexts)
                }

                startActivity(intent)
            } else {
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun generateTTSFilesForAllTexts(nom: String?, apikey: String?,intention:Boolean) {
        // Dans cet exemple, on ne génère que pour les 2 premiers textes
        for (index in 0..1) {
            if (index < userTexts.size) {
                val text = userTexts[index]
                if (nom != null && apikey != null && intention==false) {
                    textToSpeech(text, nom, index, apikey)
                }else{
                    if(intention==true && apikey != null){
                        textToSpeechIntention(text, index, apikey)

                    }
                }

            }
        }
    }

    private fun updateTextNumbers(intention:Boolean) {
        var count = 1
        for (i in 0 until container.childCount) {
            val linearLayout = container.getChildAt(i) as LinearLayout
            val editText = linearLayout.getChildAt(0) as? EditText
            if(intention){
                editText?.hint = "Intention $count"

            }else{
                editText?.hint = "Je \"affirmation $count\""

            }
            count++
        }
        textViewCount = count - 1
    }

    private fun generateAudioFileForText(text: String, index: Int) {
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val generatedFilePath = "$basePath/voice_$index.mp3"
        val file = File(generatedFilePath)
        generateFiles.add(generatedFilePath)

        textToSpeech.synthesizeToFile(text, null, file, null)
        Toast.makeText(this, "Fichier audio généré pour le texte $index", Toast.LENGTH_SHORT).show()
    }

   /* private fun showAddTextDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ajouter un texte")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Valider") { dialogInterface, _ ->
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                userTexts.add(text)
                addTextView(text, userTexts)
            }
        }
        builder.setNegativeButton("Annuler") { dialogInterface, _ -> dialogInterface.cancel() }

        val dialog = builder.create()
        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        customizeDialogButton(positiveButton)
        customizeDialogButton(negativeButton)
        addSpaceBetweenDialogButtons(positiveButton, negativeButton)
    }*/

    private fun customizeDialogButton(button: Button) {
        button.setBackgroundColor(Color.parseColor("#27c485"))
        button.setTextColor(Color.WHITE)

        val paddingInDp = 15
        val scale = resources.displayMetrics.density
        val paddingInPx = (paddingInDp * scale + 0.5f).toInt()
        button.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)

        val drawable = ContextCompat.getDrawable(this, R.drawable.button_background)
        button.background = drawable
    }

    private fun addSpaceBetweenDialogButtons(positiveButton: Button, negativeButton: Button) {
        val marginInDp = 8
        val scale = resources.displayMetrics.density
        val marginInPx = (marginInDp * scale + 0.5f).toInt()

        val positiveButtonLP = positiveButton.layoutParams as LinearLayout.LayoutParams
        val negativeButtonLP = negativeButton.layoutParams as LinearLayout.LayoutParams

        positiveButtonLP.marginStart = marginInPx
        positiveButton.layoutParams = positiveButtonLP

        negativeButtonLP.marginEnd = marginInPx
        negativeButton.layoutParams = negativeButtonLP
    }

    private fun addTextView(text: String, userText: ArrayList<String>,intention :Boolean) {
        textViewCount++
        val placeholderText: String

        if (intention) {
            placeholderText = if (text.isEmpty()) {
                "Intention $textViewCount"
            } else {
                text
            }
        } else {
            placeholderText = if (text.isEmpty()) {
                "Je \"affirmation $textViewCount\""
            } else {
                text
            }
        }


        // position du texte actuel dans userTexts : c'est le dernier ajouté
        val position = userTexts.size - 1

        val horizontalContainer = LinearLayout(this)
        horizontalContainer.orientation = LinearLayout.HORIZONTAL
        horizontalContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 16, 16, 16)
        }

        val affirmationEditText = EditText(this)
        affirmationEditText.hint = placeholderText
        affirmationEditText.textSize = 24f
        affirmationEditText.setHintTextColor(Color.parseColor("#808080"))
        affirmationEditText.setTextColor(Color.parseColor("#333333"))
        affirmationEditText.setBackgroundResource(R.drawable.rounded_corners)
        affirmationEditText.setPadding(16, 40, 16, 40)
        affirmationEditText.textAlignment = View.TEXT_ALIGNMENT_CENTER
        affirmationEditText.setTypeface(null, Typeface.ITALIC)

        val layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        layoutParams.setMargins(0, 30, 0, 0)
        affirmationEditText.layoutParams = layoutParams

        // Mettre à jour le style du texte et la liste userTexts en temps réel
        affirmationEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    affirmationEditText.setTypeface(null, Typeface.NORMAL)
                } else {
                    affirmationEditText.setTypeface(null, Typeface.ITALIC)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                // Met à jour userTexts à chaque changement
                userTexts[position] = s.toString()
            }
        })

        val deleteButton = ImageButton(this)
        deleteButton.setImageResource(R.drawable.croixgris)
        deleteButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 30, 0, 0)
        }
        deleteButton.setBackgroundColor(Color.TRANSPARENT)

        deleteButton.setOnClickListener {
            val position = userTexts.indexOf(text) // Récupère la position exacte
            if (position >= 0 && position < userTexts.size) {
                userTexts.removeAt(position)
                container.removeView(horizontalContainer)
                updateTextNumbers(intention)
            } else {
                Log.e("Error", "Position invalide pour la suppression : $position")
            }
        }

        horizontalContainer.addView(affirmationEditText)
        horizontalContainer.addView(deleteButton)
        container.addView(horizontalContainer)
    }

    private fun textToSpeech(text: String, nom: String, index: Int, voiceId: String) {
        val apiKey = "sk_1e85a97e6cdd33e449f8578f3fa7152594bdab061b0649b7" // Remplace avec ta clé API
        val client = OkHttpClient()
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val generatedFilePath = "$basePath/voice_$index.mp3"
        generateFiles.add(generatedFilePath)


        val fullText = "moi $nom, $text."

        Log.d("test1234", "textToSpeech: "+fullText)
        Log.i("test1234", "textToSpeech: "+fullText)

        val bodyJson = JSONObject().apply {
            put("text", fullText)
            put("model_id", "eleven_turbo_v2_5")
            put("language_code", "fr")
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("testApi", "Erreur lors de l'appel API : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body
                if (responseBody != null) {
                    val audioFile = File(generatedFilePath)
                    try {
                        val outputStream = FileOutputStream(audioFile)
                        outputStream.write(responseBody.bytes())
                        outputStream.close()

                        Log.d("testApi123", "Fichier audio sauvegardé à : ${audioFile.absolutePath}")

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


    private fun textToSpeechIntention(text: String, index: Int, voiceId: String) {
        val apiKey = "sk_1e85a97e6cdd33e449f8578f3fa7152594bdab061b0649b7" // Remplace avec ta clé API
        val client = OkHttpClient()
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val generatedFilePath = "$basePath/voice_$index.mp3"
        generateFiles.add(generatedFilePath)


        val fullText = "$text."

        Log.d("test1234", "textToSpeech: "+fullText)
        Log.i("test1234", "textToSpeech: "+fullText)

        val bodyJson = JSONObject().apply {
            put("text", fullText)
            put("model_id", "eleven_turbo_v2_5")
            put("language_code", "fr")
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("testApi", "Erreur lors de l'appel API : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body
                if (responseBody != null) {
                    val audioFile = File(generatedFilePath)
                    try {
                        val outputStream = FileOutputStream(audioFile)
                        outputStream.write(responseBody.bytes())
                        outputStream.close()

                        Log.d("testApi123", "Fichier audio sauvegardé à : ${audioFile.absolutePath}")

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

}
