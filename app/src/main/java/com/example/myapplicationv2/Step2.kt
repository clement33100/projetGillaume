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
import android.text.InputFilter
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
import android.text.InputType
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat


class Step2 : Base() {
    private lateinit var container: LinearLayout
    private var textViewCount = 0
    private lateinit var buttonOk: Button
    private lateinit var addButton: Button
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

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Initialisation de TextToSpeech
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

        buttonOk = findViewById(R.id.step2ok)
        addButton = findViewById(R.id.addButton)
        container = findViewById(R.id.container)
        val btnShowAdvices = findViewById<Button>(R.id.btn_show_advices)
        val scrollView = findViewById<ScrollView>(R.id.scrollViewAffirm)
        var isAdviceExpanded = false
        btnShowAdvices.isAllCaps = false

        // Préparation du texte de conseils
        val title = formatHtmlText("<b>Conseils Pratiques</b>", 0.95f)
        val advice1 = formatHtmlText("<b>FORMULE AU PRÉSENT</b> comme si c’était une réalité. <i>\"Je suis confiant.\"</i>", 0.75f)
        val advice2 = formatHtmlText("<b>SOIS POSITIF</b> en te concentrant sur ce que tu veux, pas sur ce que tu veux éviter", 0.75f)
        val advice3 = formatHtmlText("<b>CHOISIS TES MOTS</b> riches de sens pour toi", 0.75f)
        val advice6 = formatHtmlText("<b>SURMONTE TES RÉSISTANCES</b> avec : <i>\"Je m’ouvre à la possibilité de... .\"</i>", 0.75f)

        // Dans votre méthode onCreate, après avoir récupéré vos vues
        val rootView = findViewById<View>(R.id.drawer_layout)  // Vue racine globale
        val constraintLayout = findViewById<ConstraintLayout>(R.id.main)
        val scrollViewa = findViewById<ScrollView>(R.id.scrollViewAffirm)

// Déclaration de addButton et buttonOk, qui doivent déjà être initialisées par findViewById
// Par exemple :
// val addButton = findViewById<Button>(R.id.addButton)
// val buttonOk = findViewById<Button>(R.id.step2ok)

// Déclaration du ConstraintSet original pour restaurer l'état initial quand le clavier est fermé
        val originalConstraintSet = ConstraintSet()
        originalConstraintSet.clone(constraintLayout)

// Listener pour détecter l'ouverture et la fermeture du clavier
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            // Si la hauteur du clavier dépasse 15% de la hauteur totale de l'écran
            if (keypadHeight > screenHeight * 0.15) {
                // Clavier affiché : masquer les boutons et fixer leur hauteur à 0
                addButton.visibility = View.GONE
                buttonOk.visibility = View.GONE
                addButton.layoutParams.height = 0
                buttonOk.layoutParams.height = 0

                // Modifier les contraintes pour que le ScrollView s'étende jusqu'en bas
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)
                constraintSet.connect(
                    scrollViewa.id, ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0
                )
                constraintSet.applyTo(constraintLayout)
                constraintLayout.requestLayout()
            } else {
                // Clavier caché : afficher les boutons et restaurer leur hauteur d'origine
                addButton.visibility = View.VISIBLE
                buttonOk.visibility = View.VISIBLE
                addButton.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                buttonOk.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

                // Restaurer la configuration initiale enregistrée dans originalConstraintSet
                originalConstraintSet.applyTo(constraintLayout)
                constraintLayout.requestLayout()
            }
        }
        var collapsedWidth = 0
        btnShowAdvices.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Ici, le bouton affiche seulement "Conseils Pratiques"
                collapsedWidth = btnShowAdvices.width
                btnShowAdvices.minWidth = collapsedWidth
                btnShowAdvices.maxWidth = collapsedWidth
                btnShowAdvices.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        btnShowAdvices.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_advices, null)
            val tvAdvices = dialogView.findViewById<TextView>(R.id.tvAdvices)

            val adviceText = SpannableStringBuilder().apply {
                append(formatHtmlText("<b>FORMULE AU PRÉSENT</b> comme si c’était une réalité. <i>\"Je suis confiant.\"</i>", 0.9f))
                append("\n\n")
                append(formatHtmlText("<b>SOIS POSITIF</b> en te concentrant sur ce que tu veux, pas sur ce que tu veux éviter", 0.9f))
                append("\n\n")
                append(formatHtmlText("<b>CHOISIS TES MOTS</b> riches de sens pour toi", 0.9f))
                append("\n\n")
                append(formatHtmlText("<b>SURMONTE TES RÉSISTANCES</b> avec : <i>\"Je m’ouvre à la possibilité de... .\"</i>", 0.9f))
            }

            tvAdvices.text = adviceText

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Fermer") { d, _ -> d.dismiss() }
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Pour voir les coins arrondis
            dialog.show()
        }

        // Gestion des insets
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom + imeInsets.bottom
            )
            insets
        }

        val curentVoice = intent.getStringExtra("curentVoice")
        val curentAPIKey = intent.getStringExtra("curentAPIKey")

        titleStep2 = findViewById(R.id.titlestep2)

        val predefinedTexts = listOf("Affirmation 1", "Affirmation 2")
        predefinedTexts.forEach {
            userTexts.add(it) // On ajoute le texte à userTexts
            addTextView(it, userTexts) // On affiche dans le layout
        }

        addButton.setOnClickListener {
            if (userTexts.size < 8) {
                // Ajout d'une nouvelle affirmation vide
                val newText = ""
                userTexts.add(newText)
                addTextView(newText, userTexts)
            } else {
                Toast.makeText(this, "Vous avez atteint le nombre maximum d'affirmations (8).", Toast.LENGTH_SHORT).show()
            }
        }

        buttonOk.setOnClickListener {
            // userTexts est déjà mis à jour grâce aux TextWatcher
            // ─── 1. Vérifier si les deux affirmations de base sont inchangées ───
            val def1 = "Affirmation 1"   // valeurs que tu crées au démarrage
            val def2 = "Affirmation 2"

            val base1Unchanged = userTexts.getOrNull(0).isNullOrBlank() || userTexts[0] == def1
            val base2Unchanged = userTexts.getOrNull(1).isNullOrBlank() || userTexts[1] == def2

            if (base1Unchanged && base2Unchanged) {
                Toast.makeText(
                    this,
                    "Modifie au moins l’une des deux affirmations proposées avant de continuer.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener        // ⬅ stop : on ne continue pas
            }

            generateTTSFilesForAllTexts(curentAPIKey)

            if(userTexts.size==0){
                Toast.makeText(this, "Vous devez selectionner au moins une affirmation positive", Toast.LENGTH_SHORT).show()
            }else{
                if (curentVoice != null) {
                    val intent = Intent(this, step3Music::class.java)
                    intent.putExtra("curentVoice", curentVoice)
                    intent.putStringArrayListExtra("userTexts", generateFiles)

                    if (userTexts.size > 3) {
                        intent.putExtra("curentAPIKey", curentAPIKey)
                        intent.putStringArrayListExtra("userTextsSplit", userTexts)
                    }

                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    /**
     * Génère les fichiers audio TTS pour tous les textes.
     *
     * @param apikey Clé API pour le service TTS.
     */
    private fun generateTTSFilesForAllTexts(apikey: String?) {
        val placeholderPattern = Regex("^Affirmation\\s+\\d+$")

        for ((index, text) in userTexts.withIndex()) {
            // Vérifie si le texte n'est pas vide ET ne correspond pas au placeholder par défaut
            if (!text.isNullOrBlank() && !placeholderPattern.matches(text)) {
                if (apikey != null) {
                    textToSpeech(text, index, apikey)
                }
            } else {
                Log.d("TextSkipped", "Affirmation ignorée : '$text'")
            }
        }
    }

    /**
     * Met à jour les numéros des TextViews après suppression.
     */
    private fun updateTextNumbers() {
        var count = 1
        for (i in 0 until container.childCount) {
            val linearLayout = container.getChildAt(i) as LinearLayout
            val editText = linearLayout.getChildAt(0) as? EditText
            editText?.hint = "Affirmation $count"
            count++
        }
        textViewCount = count - 1
    }

    /**
     * Ajoute une nouvelle TextView pour une affirmation ou une intention.
     */
    private fun addTextView(text: String, userText: ArrayList<String>) {
        textViewCount++
        val placeholderText = if (text.isEmpty()) {
            "Affirmation $textViewCount"
        } else {
            text
        }

        // Position du texte actuel dans userTexts : c'est le dernier ajouté
        val position = userText.size - 1

        val horizontalContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 16, 16, 16) }
        }

        val affirmationEditText = EditText(this).apply {
            hint = placeholderText
            textSize = 24f
            setHintTextColor(Color.parseColor("#808080"))
            setTextColor(Color.parseColor("#333333"))
            setBackgroundResource(R.drawable.rounded_corners)
            setPadding(16, 40, 16, 40)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTypeface(null, Typeface.ITALIC)

            // ── Limite à 177 caractères, avec toast en cas de dépassement ──
            val maxChars = 177
            filters = arrayOf(object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned,
                    dstart: Int,
                    dend: Int
                ): CharSequence? {
                    val currentLength = dest.length
                    val replacingLength = dend - dstart
                    val newChunkLength = end - start
                    val resultingLength = currentLength - replacingLength + newChunkLength

                    return if (resultingLength > maxChars) {
                        // Bloquer le reste et afficher le Toast
                        Toast.makeText(
                            this@Step2,
                            "Oups, ton affirmation est trop longue\nLimite : 177 caractères (≈ 25–35 mots)",
                            Toast.LENGTH_SHORT
                        ).show()
                        ""  // on ne laisse rien passer au-delà de 177 caractères
                    } else {
                        null  // on autorise la saisie
                    }
                }
            })

            // Options IME pour bouton "OK"
            imeOptions = EditorInfo.IME_ACTION_DONE
            inputType = InputType.TYPE_CLASS_TEXT
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    true
                } else {
                    false
                }
            }
        }

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
                userText[position] = s.toString()
            }
        })

        val deleteButton = ImageButton(this).apply {
            setImageResource(R.drawable.croix_jaune_fusion)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 30, 0, 0) }
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                val pos = container.indexOfChild(horizontalContainer)
                if (pos != -1 && pos < userText.size) {
                    userText.removeAt(pos)
                    container.removeView(horizontalContainer)
                    updateTextNumbers()
                } else {
                    Log.e("Error", "Position invalide pour la suppression : $pos")
                }
            }
        }

        // Mise en place des LayoutParams pour l’EditText
        affirmationEditText.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply { setMargins(0, 30, 0, 0) }

        horizontalContainer.addView(affirmationEditText)
        horizontalContainer.addView(deleteButton)
        container.addView(horizontalContainer)
    }



    /**
     * Génère un fichier audio TTS pour un texte spécifique sans ajouter le nom.
     */
    private fun textToSpeech(text: String, index: Int, voiceId: String) {
        val apiKey = BuildConfig.ELEVENLABS_API_KEY
        val client = OkHttpClient()
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val generatedFilePath = "$basePath/voice_$index.mp3"
        generateFiles.add(generatedFilePath)

        // Construction du texte complet sans ajouter le nom
        val fullText = "$text."

        Log.d("test1234", "textToSpeech: $fullText")
        Log.i("test1234", "textToSpeech: $fullText")

        val bodyJson = JSONObject().apply {
            put("text", fullText)
            put("model_id", "eleven_multilingual_v2")
            put("voice_settings", JSONObject().apply {
                put("stability", 0.5)  // Mettre une valeur entre 0.0 et 1.0
                put("similarity_boost", 0.77)  // Conversion de 77 à 0.77
                put("style_exaggeration", 0.07)  // Conversion de 7 à 0.07
                put("speaker_boost", true)  // Laisser speaker_boost activé
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
                            //Toast.makeText(this@Step2, "Fichier audio généré pour l'index $index", Toast.LENGTH_SHORT).show()
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