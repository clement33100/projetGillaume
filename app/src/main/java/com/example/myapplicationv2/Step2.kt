package com.example.myapplicationv2

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.*
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
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
    private lateinit var addButton: Button
    private val userTexts = ArrayList<String>()         // Textes saisis
    private lateinit var textToSpeechEngine: TextToSpeech
    private val generatedFiles = ArrayList<String>()    // Chemins des fichiers audio générés
    private lateinit var titleStep2: TextView

    override fun getLayoutId(): Int = R.layout.activity_step2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Init TTS Android (utilisé seulement pour lecture locale éventuelle)
        textToSpeechEngine = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeechEngine.language = Locale.FRANCE
                textToSpeechEngine.setPitch(1.0f)
            }
        }

        // Helpers de mise en forme
        fun formatText(text: String, sizeMultiplier: Float, isBold: Boolean = false): SpannableString {
            val spannable = SpannableString(text)
            spannable.setSpan(RelativeSizeSpan(sizeMultiplier), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (isBold) spannable.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

        fun formatHtmlText(htmlText: String, sizeMultiplier: Float = 1.0f): SpannableStringBuilder {
            val spannable = SpannableStringBuilder(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY))
            spannable.setSpan(RelativeSizeSpan(sizeMultiplier), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

        // Views
        buttonOk = findViewById(R.id.step2ok)
        addButton = findViewById(R.id.addButton)
        container = findViewById(R.id.container)
        val btnShowAdvices = findViewById<Button>(R.id.btn_show_advices)
        val scrollViewAffirm = findViewById<ScrollView>(R.id.scrollViewAffirm)
        btnShowAdvices.isAllCaps = false

        // Préparation du bouton "Conseils pratiques" pour rester sur une seule ligne
        var collapsedWidth = 0
        btnShowAdvices.viewTreeObserver.addOnGlobalLayoutListener {
            if (collapsedWidth == 0) {
                collapsedWidth = btnShowAdvices.width
                btnShowAdvices.minWidth = collapsedWidth
                btnShowAdvices.maxWidth = collapsedWidth
            }
        }

        // Dialog "Conseils pratiques"
        btnShowAdvices.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_advices, null)
            val tvAdvices = dialogView.findViewById<TextView>(R.id.tvAdvices)
            val adviceText = SpannableStringBuilder().apply {
                append(formatHtmlText(getString(R.string.affirmation_present), 0.9f)); append("\n\n")
                append(formatHtmlText(getString(R.string.affirmation_form), 0.9f)); append("\n\n")
                append(formatHtmlText(getString(R.string.affirmation_positive), 0.9f)); append("\n\n")
                append(formatHtmlText(getString(R.string.affirmation_words), 0.9f)); append("\n\n")
                append(formatHtmlText(getString(R.string.affirmation_resistance), 0.9f))
            }
            tvAdvices.text = adviceText

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Fermer") { d, _ -> d.dismiss() }
                .create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        // Insets (barres système + clavier)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + imeInsets.bottom)
            insets
        }

        // Récup params
        val currentVoiceId = intent.getStringExtra("curentVoice")
        val currentApiKey = intent.getStringExtra("curentAPIKey")
        Log.d("Step2", "curentAPIKey=$currentApiKey, curentVoice=$currentVoiceId")

        titleStep2 = findViewById(R.id.titlestep2)

        // Placeholders init
        val predefinedTexts = listOf("Affirmation 1", "Affirmation 2")
        predefinedTexts.forEach {
            userTexts.add(it)
            addTextRow(it, userTexts)
        }

        // + Affirmation
        addButton.setOnClickListener {
            if (userTexts.size < 6) {
                val newText = ""
                userTexts.add(newText)
                addTextRow(newText, userTexts)
            } else {
                Toast.makeText(this, "Vous avez atteint le nombre maximum d'affirmations (6).", Toast.LENGTH_SHORT).show()
            }
        }

        // OK → génération TTS + navigation
        buttonOk.setOnClickListener {
            val def1 = "Affirmation 1"
            val def2 = "Affirmation 2"
            val base1Unchanged = userTexts.getOrNull(0).isNullOrBlank() || userTexts[0] == def1
            val base2Unchanged = userTexts.getOrNull(1).isNullOrBlank() || userTexts[1] == def2

            if (base1Unchanged && base2Unchanged) {
                Toast.makeText(this, getString(R.string.toastModifAffirm), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Génère les audios pour les textes valides
            generateTTSFilesForAllTexts(currentVoiceId, currentApiKey)

            if (userTexts.isEmpty()) {
                Toast.makeText(this, "Vous devez sélectionner au moins une affirmation positive", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentVoiceId != null) {
                val intent = Intent(this, step3Music::class.java).apply {
                    putExtra("curentVoice", currentVoiceId)
                    putStringArrayListExtra("userTexts", generatedFiles)

                    if (userTexts.size > 3) {
                        putExtra("curentAPIKey", currentApiKey)
                        putStringArrayListExtra("userTextsSplit", userTexts)
                    }
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Voice ID manquant : impossible de générer l'audio.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Listener clavier sécurisé (évite NPE si l'id de la ScrollView change) ---
        val rootView = findViewById<View>(R.id.drawer_layout)
        val constraintLayout = findViewById<ConstraintLayout>(R.id.main)
        val originalConstraintSet = ConstraintSet().apply { clone(constraintLayout) }

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            val keyboardOpen = keypadHeight > screenHeight * 0.15

            if (keyboardOpen) {
                addButton.visibility = View.GONE
                buttonOk.visibility = View.GONE
                addButton.layoutParams.height = 0
                buttonOk.layoutParams.height = 0

                if (scrollViewAffirm != null) {
                    val cs = ConstraintSet()
                    cs.clone(constraintLayout)
                    cs.connect(scrollViewAffirm.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                    cs.applyTo(constraintLayout)
                }
                constraintLayout.requestLayout()
            } else {
                addButton.visibility = View.VISIBLE
                buttonOk.visibility = View.VISIBLE
                addButton.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                buttonOk.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

                originalConstraintSet.applyTo(constraintLayout)
                constraintLayout.requestLayout()
            }
        }
        // (Pense à mettre android:windowSoftInputMode="adjustResize" sur l'activité Step2 dans le Manifest)
    }

    /**
     * Ajoute une ligne (EditText + bouton suppression) sans capturer d’index.
     * L’index est toujours retrouvé via la position de la vue dans 'container'.
     */
    private fun addTextRow(text: String, userText: ArrayList<String>) {
        textViewCount++
        val placeholderText = if (text.isEmpty()) "Affirmation $textViewCount" else text

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 16, 16, 16) }
        }

        val et = EditText(this).apply {
            hint = placeholderText
            textSize = 24f
            setHintTextColor(Color.parseColor("#808080"))
            setTextColor(Color.parseColor("#333333"))
            setBackgroundResource(R.drawable.rounded_corners)
            setPadding(16, 40, 16, 40)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTypeface(null, Typeface.ITALIC)

            // Limite 90 caractères + toast
            val maxChars = 90
            filters = arrayOf(object : InputFilter {
                override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
                    val currentLength = dest.length
                    val replacingLength = dend - dstart
                    val newChunkLength = end - start
                    val resultingLength = currentLength - replacingLength + newChunkLength
                    return if (resultingLength > maxChars) {
                        Toast.makeText(this@Step2, "Oups, ton affirmation est trop longue\nLimite : 90 caractères (≈ 12–15 mots)", Toast.LENGTH_SHORT).show()
                        ""
                    } else null
                }
            })

            imeOptions = EditorInfo.IME_ACTION_DONE
            inputType = InputType.TYPE_CLASS_TEXT
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    true
                } else false
            }
        }

        // TextWatcher robuste (ne capture pas d’index)
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                et.setTypeface(null, if (s.isNullOrEmpty()) Typeface.ITALIC else Typeface.NORMAL)
            }
            override fun afterTextChanged(s: Editable?) {
                val pos = container.indexOfChild(row)
                if (pos in 0 until userText.size) {
                    userText[pos] = s?.toString() ?: ""
                } else {
                    Log.e("Step2", "afterTextChanged: index $pos hors limites (size=${userText.size})")
                }
            }
        })

        val deleteBtn = ImageButton(this).apply {
            setImageResource(R.drawable.croix_verte_fusion)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 30, 0, 0) }
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                val pos = container.indexOfChild(row)
                if (pos in 0 until userText.size) {
                    userText.removeAt(pos)
                    container.removeView(row)
                    updateTextNumbers()
                } else {
                    Log.e("Step2", "Suppression: position $pos invalide (size=${userText.size})")
                }
            }
        }

        et.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(0, 30, 0, 0)
        }

        row.addView(et)
        row.addView(deleteBtn)
        container.addView(row)
        updateTextNumbers()
    }

    /** Renumérote les hints "Affirmation X" pour les champs vides après ajout/suppression. */
    private fun updateTextNumbers() {
        var count = 1
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i) as? LinearLayout ?: continue
            val et = row.getChildAt(0) as? EditText ?: continue
            if (et.text.isNullOrEmpty()) et.hint = "Affirmation $count"
            count++
        }
        textViewCount = count - 1
    }

    /**
     * Génère les fichiers audio TTS ElevenLabs pour tous les textes valides.
     * Utilise 'voiceId' pour l'URL et 'apiKey' (ou BuildConfig) pour l'authent.
     */
    private fun generateTTSFilesForAllTexts(voiceId: String?, apiKey: String?) {
        if (voiceId.isNullOrBlank()) {
            Log.e("Step2", "VoiceId manquant: génération TTS annulée")
            return
        }
        val finalApiKey = apiKey ?: BuildConfig.ELEVENLABS_API_KEY

        // Nettoyage / création du dossier audio une seule fois
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (audioDir.exists()) {
            audioDir.listFiles()?.forEach { it.delete() }
        } else {
            audioDir.mkdirs()
        }
        generatedFiles.clear()

        // Pattern pour ignorer "Affirmation X"
        val placeholderPattern = Regex("^Affirmation\\s+\\d+$")

        userTexts.forEachIndexed { index, text ->
            if (!text.isNullOrBlank() && !placeholderPattern.matches(text)) {
                textToSpeechRemote(text, index, voiceId, finalApiKey, basePath)
            } else {
                Log.d("Step2", "Affirmation ignorée : '$text'")
            }
        }
    }

    /**
     * Appel ElevenLabs (OkHttp) pour générer l'audio.
     */
    private fun textToSpeechRemote(text: String, index: Int, voiceId: String, apiKey: String, basePath: String) {
        val client = OkHttpClient()

        val generatedFilePath = "$basePath/voice_$index.mp3"
        generatedFiles.add(generatedFilePath)

        val fullText = "$text."
        val bodyJson = JSONObject().apply {
            put("text", fullText)
            put("model_id", "eleven_multilingual_v2")
            put("voice_settings", JSONObject().apply {
                put("stability", 0.5)
                put("similarity_boost", 0.77)
                put("style_exaggeration", 0.07)
                put("speaker_boost", true)
            })
        }

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), bodyJson.toString())
        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
            .addHeader("Content-Type", "application/json")
            .addHeader("xi-api-key", apiKey)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Step2", "Erreur API ElevenLabs : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body
                if (responseBody != null) {
                    val audioFile = File(generatedFilePath)
                    try {
                        FileOutputStream(audioFile).use { it.write(responseBody.bytes()) }
                        Log.d("Step2", "Audio sauvegardé : ${audioFile.absolutePath}")
                    } catch (e: IOException) {
                        Log.e("Step2", "Erreur sauvegarde audio : ${e.message}")
                    }
                } else {
                    Log.e("Step2", "Réponse ElevenLabs vide (body null)")
                }
            }
        })
    }
}
