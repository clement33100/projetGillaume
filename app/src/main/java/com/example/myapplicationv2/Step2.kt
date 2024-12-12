package com.example.myapplicationv2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.media.Image
import android.os.Bundle
import android.os.Environment
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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
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

        // Configuration du bouton de navigation dans Base, ne plus toucher à btn_burger iciee


        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.FRANCE
                textToSpeech.setPitch(1.0f)
            }
        }

        // Fonction pour créer du texte formaté
        fun formatText(text: String, sizeMultiplier: Float, isBold: Boolean = false): SpannableString {
            val spannable = SpannableString(text)
            spannable.setSpan(RelativeSizeSpan(sizeMultiplier), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (isBold) {
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return spannable
        }
        fun formatHtmlText(htmlText: String, sizeMultiplier: Float = 1.0f): SpannableStringBuilder {
            // Convertir le HTML en Spannable
            val spannable = SpannableStringBuilder(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY))

            // Appliquer la taille relative à tout le texte
            spannable.setSpan(
                RelativeSizeSpan(sizeMultiplier),
                0,
                spannable.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            return spannable
        }

// Dans votre Activity ou Fragment
        val btnShowAdvices = findViewById<Button>(R.id.btn_show_advices)
        val scrollView = findViewById<ScrollView>(R.id.scrollViewAffirm)
        var isAdviceExpanded = false
        btnShowAdvices.isAllCaps = false

// Préparation du texte
        val title = formatHtmlText("<b>CONSEIL PRATIQUE</b>", 1f)
        val advice1 = formatHtmlText("<b>FORMULE AU PRÉSENT</b> comme si c’était une réalité. <i>\"Je suis confiant.\"</i>", 0.8f)
        val advice2 = formatHtmlText("<b>SOIS POSITIF</b> en te concentrant sur ce que tu veux, pas sur ce que tu veux éviter", 0.8f)
        val advice3 = formatHtmlText("<b>CHOISIS TES MOTS</b> riches de sens pour toi", 0.8f)
        val advice4 = formatHtmlText("<b>SOIS CLAIR</b>, simple et précis", 0.8f)
        val advice5 = formatHtmlText("<b>RENFORCE TES AFFIRMATIONS</b> avec des exemples concrets de ta vie qui les confirment : <i>\"Je suis confiant parce que j’ai déjà atteint 'cet objectif' que je m’étais fixé.\"</i>", 0.8f)

// Dessins pour les flèches haut/bas
        val arrowUpDrawable = ContextCompat.getDrawable(this, R.drawable.arrowbottomtom)
        val insetArrow = InsetDrawable(arrowUpDrawable, 0, -28, 0, 0)
        insetArrow?.setBounds(0, 0, insetArrow.intrinsicWidth, insetArrow.intrinsicHeight)

        val arrowDownDrawable = ContextCompat.getDrawable(this, R.drawable.arrowdown)
        arrowDownDrawable?.setBounds(0, 0, arrowDownDrawable.intrinsicWidth, arrowDownDrawable.intrinsicHeight)


        btnShowAdvices.setOnClickListener {
            if (!isAdviceExpanded) {
                // État déplié : montrer tout le texte
                btnShowAdvices.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) // Retire la flèche du bouton lui-même

                // On utilise SpannableStringBuilder pour construire un texte complet
                val finalTextBuilder = SpannableStringBuilder()
                finalTextBuilder.append(title)

                // Insérer un espace + l'image flèche haut
                if (arrowUpDrawable != null) {
                    finalTextBuilder.append("              ")
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
                finalTextBuilder.append(advice5)

                // Appliquer le texte complet
                btnShowAdvices.text = finalTextBuilder

                // Ajuste la ScrollView après l'expansion
                btnShowAdvices.post {
                    scrollView.post {
                        scrollView.smoothScrollTo(0, btnShowAdvices.bottom)
                    }
                }

            } else {
                // État réduit : uniquement le titre
                val minimalTextBuilder = SpannableStringBuilder()
                minimalTextBuilder.append(title)

                // Définir l'image à droite du texte grâce aux compoundDrawables
                btnShowAdvices.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowdown, 0)

                btnShowAdvices.text = minimalTextBuilder

                // Revenir à la position initiale
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
        val curentAPIKey = intent.getStringExtra("curentAPIKey")


        val predefinedTexts = listOf("Je \"affirmation 1\"", "Je \"affirmation 2\"")
        predefinedTexts.forEach {
            userTexts.add(it) // Add to list
            addTextView(it) // Display in layout
        }
        buttonOk.setOnClickListener{

            generateTTSFilesForAllTexts(nom,curentAPIKey)

            //textToSpeech(userTexts.get(0),"VR6AewLTigWG4xSOukaG")

            if(curentVoice!=null){
                val intent = Intent(this, step3Music::class.java)
                intent.putExtra("curentVoice", curentVoice)
                intent.putStringArrayListExtra("userTexts", generateFiles)

                if(userTexts.size>2){
                    intent.putExtra("curentAPIKey", curentAPIKey)
                    intent.putExtra("nom", nom)
                    intent.putStringArrayListExtra("userTextsSplit", userTexts)
                }

                startActivity(intent)


            }else{
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()

            }


        }


        addButton.setOnClickListener {
            Log.d("Step2", "Button clicked")
            addTextView("")
        }
    }



    private fun generateTTSFilesForAllTexts(nom: String?, apikey: String?) {
        for (index in 0..1) {
            if (index < userTexts.size) {
                val text = userTexts[index]
                if (nom != null && apikey != null) {
                    textToSpeech(text, nom, index, apikey)
                }
            }
        }
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
        builder.setPositiveButton("Valider") { dialogInterface, which ->
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                userTexts.add(text)  // Ajouter le texte à la liste
                addTextView(text)
            }
        }
        builder.setNegativeButton("Annuler") { dialogInterface, which -> dialogInterface.cancel() }

        // Créer le dialog
        val dialog = builder.create()

        // Afficher le dialog
        dialog.show()

        // Personnaliser les boutons après affichage
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        // Appliquer le style aux boutons
        customizeDialogButton(positiveButton)
        customizeDialogButton(negativeButton)
        addSpaceBetweenDialogButtons(positiveButton, negativeButton)
    }

    private fun customizeDialogButton(button: Button) {
        // Définir la couleur de fond
        button.setBackgroundColor(Color.parseColor("#27c485")) // Couleur verte

        // Définir la couleur du texte
        button.setTextColor(Color.WHITE)

        // Ajouter du padding (convertir 15dp en pixels)
        val paddingInDp = 15
        val scale = resources.displayMetrics.density
        val paddingInPx = (paddingInDp * scale + 0.5f).toInt()
        button.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)

        // Appliquer un style de coin arrondi si nécessaire
        val drawable = ContextCompat.getDrawable(this, R.drawable.button_background)
        button.background = drawable
    }
    private fun addSpaceBetweenDialogButtons(positiveButton: Button, negativeButton: Button) {
        // Convertir 8dp en pixels pour les marges
        val marginInDp = 8
        val scale = resources.displayMetrics.density
        val marginInPx = (marginInDp * scale + 0.5f).toInt()

        // Récupérer les LayoutParams des boutons
        val positiveButtonLP = positiveButton.layoutParams as LinearLayout.LayoutParams
        val negativeButtonLP = negativeButton.layoutParams as LinearLayout.LayoutParams

        // Ajouter une marge à gauche du bouton positif (Valider)
        positiveButtonLP.marginStart = marginInPx
        positiveButton.layoutParams = positiveButtonLP

        // Si vous le souhaitez, vous pouvez également ajouter une marge à droite du bouton négatif (Annuler)
        negativeButtonLP.marginEnd = marginInPx
        negativeButton.layoutParams = negativeButtonLP
    }

    private fun addTextView(text: String) {
        textViewCount++
        val placeholderText = if (text.isEmpty()) {
            "Je \"affirmation $textViewCount\""
        } else {
            text
        }

        // Crée un conteneur horizontal pour l'EditText et le ImageButton
        val horizontalContainer = LinearLayout(this)
        horizontalContainer.orientation = LinearLayout.HORIZONTAL
        horizontalContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 16, 16, 16) // Marges autour du conteneur
        }

// Crée un EditText pour l'affirmation
        val affirmationEditText = EditText(this)
        affirmationEditText.hint = placeholderText
        affirmationEditText.textSize = 24f
        affirmationEditText.setHintTextColor(Color.parseColor("#808080")) // Couleur grise pour le hint
        affirmationEditText.setTextColor(Color.parseColor("#333333")) // Couleur du texte
        affirmationEditText.setBackgroundResource(R.drawable.rounded_corners) // Fond avec coins arrondis
        affirmationEditText.setPadding(16, 40, 16, 40) // Padding interne
        affirmationEditText.textAlignment = View.TEXT_ALIGNMENT_CENTER
        affirmationEditText.setTypeface(null, android.graphics.Typeface.ITALIC) // Texte en italique
        val layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f // L'EditText prend tout l'espace disponible
        )
        layoutParams.setMargins(0, 30, 0, 0) // Marge supérieure de 30dp
        affirmationEditText.layoutParams = layoutParams

        // Ajoute un TextWatcher pour gérer le style du texte
        affirmationEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    // Retire l'italique lorsque du texte est saisi
                    affirmationEditText.setTypeface(null, android.graphics.Typeface.NORMAL)
                } else {
                    // Remet l'italique lorsque le champ est vide
                    affirmationEditText.setTypeface(null, android.graphics.Typeface.ITALIC)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Crée un ImageButton pour supprimer l'affirmation
        val deleteButton = ImageButton(this)
        deleteButton.setImageResource(R.drawable.croixgris) // Icône en forme de croix
        deleteButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 30, 0, 0) // Marges à gauche du bouton
        }
        deleteButton.setBackgroundColor(Color.TRANSPARENT) // Fond transparent
        deleteButton.setOnClickListener {
            container.removeView(horizontalContainer) // Supprime le conteneur parent
            updateTextViewNumbers() // Met à jour les numéros des affirmations
        }

        // Ajoute l'EditText et le bouton dans le conteneur horizontal
        horizontalContainer.addView(affirmationEditText)
        horizontalContainer.addView(deleteButton)

        // Ajoute le conteneur horizontal au conteneur principal
        container.addView(horizontalContainer)
    }

    // Méthode pour mettre à jour les numéros des affirmations
    private fun updateTextViewNumbers() {
        var currentNumber = 1
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is LinearLayout) {
                val editText = (child.getChildAt(0) as? EditText)
                editText?.hint = "Je \"affirmation $currentNumber\""
                currentNumber++
            }
        }
        textViewCount = currentNumber - 1 // Ajuste le compteur global
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

        val fullText = "moi $nom, $text"

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



}