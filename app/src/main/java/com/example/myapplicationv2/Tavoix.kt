package com.example.myapplicationv2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.InsetDrawable
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.common.MediaItem
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

class Tavoix : Base() {
    private lateinit var container: LinearLayout
    private var textViewCount = 0
    private lateinit var buttonOk: Button
    private lateinit var addButton: Button
    private val userTexts = ArrayList<String>()  // Liste pour stocker les textes saisis
    private lateinit var textToSpeech: TextToSpeech
    private val generateFiles = ArrayList<String>()  // Liste pour stocker les chemins des fichiers générés
    private lateinit var titleStep2: TextView
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentRecordingIndex: Int = -1
    private var recordStartTime: Long = 0L
    private var curentVoice: String? = null
    companion object {
        private const val MAX_AFFIRMATIONS = 6
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_tavoix  // Retourne le layout spécifique à cette activité
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
                append(formatHtmlText("Écris sous la forme :<b> \"Moi, [ton Prénom], je...\"</b>", 0.9f))
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



        titleStep2 = findViewById(R.id.titlestep2)

        val predefinedTexts = listOf("Affirmation 1", "Affirmation 2")
        predefinedTexts.forEach {
            userTexts.add(it) // On ajoute le texte à userTexts
            addTextView(it, userTexts) // On affiche dans le layout
        }

        addButton.setOnClickListener {
            if (container.childCount < MAX_AFFIRMATIONS) {
                addTextView("", userTexts)   // addTextView s’occupe d’ajouter dans userTexts
            } else {
                Toast.makeText(this,     getString(R.string.toast_max_affirmations, MAX_AFFIRMATIONS), Toast.LENGTH_SHORT).show()
            }
        }

        buttonOk.setOnClickListener {

            Log.d("tata", "onCreate: " + generateFiles[0].toString())

            // 1) Stoppe un éventuel enregistrement en cours pour éviter les incohérences
            if (isRecording) stopRecording(force = true)

            // 2) Vérifie qu'au moins une voix a été enregistrée (fichier réellement présent)
            val hasAtLeastOneRecording = generateFiles.any { path ->
                path.isNotBlank() && File(path).exists()
            }

            if (!hasAtLeastOneRecording) {
                Toast.makeText(
                    this,
                    R.string.toast_enregistrement,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }




            //generateTTSFilesForAllTexts(curentAPIKey) // (si tu veux attendre la fin, vois la note plus bas)
            val intent = Intent(this, step3Music::class.java)
            // → Envoi de la voix

            // → Envoi des MP3 (on filtre les chemins valides)
            val mp3s = ArrayList(
                generateFiles.filter { it.isNotBlank() && File(it).exists() }
            )
            intent.putStringArrayListExtra("userTexts", mp3s)


            startActivity(intent)

        }


    }
    private fun checkAndRequestMicPermission(onGranted: () -> Unit) {
        val perm = android.Manifest.permission.RECORD_AUDIO
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, perm)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onGranted(); return
        }
        androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(perm), 101)
        // On relance onGranted s’il est accordé dans onRequestPermissionsResult :
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Rien à relancer ici automatiquement; l’utilisateur refait l’appui long
            Toast.makeText(this, "Permission micro accordée. Maintiens le bouton pour enregistrer.", Toast.LENGTH_SHORT).show()
        } else if (requestCode == 101) {
            Toast.makeText(this, "Permission micro refusée.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun ensureAudioDir(): File {
        val dir = File(filesDir, "audio")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }


    private fun m4aPathForIndex(index: Int): String =
        File(ensureAudioDir(), "voice_$index.m4a").absolutePath

    private fun mp3PathForIndex(index: Int): String =
        File(ensureAudioDir(), "voice_$index.mp3").absolutePath

    private fun outputPathForIndex(index: Int): String {
        // Fichier natif Android recommandé : AAC/M4A
        // Si tu veux écraser/“remplacer” un TTS existant, garde le même nom
        // return File(ensureAudioDir(), "voice_$index.m4a").absolutePath

        // Variante : séparer tes enregistrements manuels pour éviter d’écraser le TTS :
        return File(ensureAudioDir(), "voice_$index.m4a").absolutePath
    }

    @SuppressLint("MissingPermission")
    private fun startRecording(index: Int) {
        if (isRecording) return
        checkAndRequestMicPermission {
            val m4aPath = m4aPathForIndex(index)
            try {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128_000)
                    setAudioSamplingRate(44_100)
                    setOutputFile(m4aPath)
                    setMaxDuration(10_000) // max 10 secondes
                    prepare()
                    start()
                }
                recordStartTime = System.currentTimeMillis() // ⏱ heure de début
                isRecording = true
                currentRecordingIndex = index

                // Sécurité : stop auto après 10s
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isRecording && currentRecordingIndex == index) {
                        stopRecording()
                        Toast.makeText(this, "Enregistrement limité à 10 secondes", Toast.LENGTH_SHORT).show()
                    }
                }, 10_000)

            } catch (e: Exception) {
                Log.e("REC", "startRecording error: ${e.message}")
                Toast.makeText(this, "Impossible de démarrer l’enregistrement", Toast.LENGTH_SHORT).show()
                stopRecording(force = true)
            }
        }
    }

    private fun stopRecording(force: Boolean = false) {
        if (!isRecording && !force) return
        try {
            mediaRecorder?.apply {
                try { stop() } catch (_: Exception) {}
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null

        val finishedIndex = currentRecordingIndex
        isRecording = false
        currentRecordingIndex = -1

        // Calcul de la durée
        val duration = System.currentTimeMillis() - recordStartTime
        if (duration < 2000) { // moins de 2 secondes
            // On supprime le fichier m4a créé
            try { File(m4aPathForIndex(finishedIndex)).delete() } catch (_: Exception) {}
            Toast.makeText(this, "Enregistrement trop court (< 2 sec), ignoré", Toast.LENGTH_SHORT).show()
            return
        }

        if (finishedIndex >= 0) {
            val inPath = m4aPathForIndex(finishedIndex)
            val outPath = inPath.replace(".m4a", ".mp3")

            val cmd = "-y -i \"$inPath\" -codec:a libmp3lame -q:a 2 \"$outPath\""
            FFmpegKit.executeAsync(cmd) { session ->
                val ok = ReturnCode.isSuccess(session.returnCode)
                runOnUiThread {
                    if (ok) {
                        try { File(inPath).delete() } catch (_: Exception) {}
                        while (generateFiles.size <= finishedIndex) generateFiles.add("")
                        generateFiles[finishedIndex] = outPath
                        //Toast.makeText(this, "Enregistré : voice_$finishedIndex.mp3", Toast.LENGTH_SHORT).show()
                        replaceMicWithPlayer(finishedIndex, outPath)
                    } else {
                        //Toast.makeText(this, "Conversion MP3 échouée", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopRecording(force = true)
        for (i in 0 until container.childCount) {
            val r = container.getChildAt(i) as? LinearLayout ?: continue
            r.findViewById<PlayerView>(R.id.player_view_inline)?.player?.release()
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

    @OptIn(UnstableApi::class)
    private fun replaceMicWithPlayer(index: Int, filePath: String) {
        val row = container.getChildAt(index) as? LinearLayout ?: return
        if (row.childCount < 2) return

        // 1) Retirer le micro (à l’index 1)
        row.removeViewAt(1)

        // 2) Gonfler le player stylé
        val playerContainer = layoutInflater.inflate(
            R.layout.player_item_view, row, false
        ) as LinearLayout

        // >>> Compact: largeur partielle de la ligne (ex: 30%)
        playerContainer.layoutParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, /* weight */ 0.3f
        ).apply { setMargins(8, 0, 8, 0) }

        val playerView = playerContainer.findViewById<PlayerView>(R.id.player_view_inline).apply {
            setControllerShowTimeoutMs(0)
            showController()
        }

        // 3) Créer le player et lier le fichier
        val player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(java.io.File(filePath))))
            exo.prepare()
            exo.playWhenReady = true
        }

        // 4) Insérer à la place du micro -> la croix reste en (nouvel) index 2
        row.addView(playerContainer, 1)
    }

    /**
     * Ajoute une nouvelle TextView pour une affirmation ou une intention.
     */
    private fun addTextView(@Suppress("UNUSED_PARAMETER") text: String, userText: ArrayList<String>) {
        if (container.childCount >= MAX_AFFIRMATIONS) {
            Toast.makeText(this, "Maximum $MAX_AFFIRMATIONS enregistrements.", Toast.LENGTH_SHORT).show()
            return
        }

        userText.add("")                           // compat
        while (generateFiles.size < container.childCount + 1) generateFiles.add("")

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 16, 16, 16) }
            gravity = Gravity.CENTER_VERTICAL
        }

        // 1) Label "Affirmation X" (affiché en premier)
        val label = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f
            ).apply { setMargins(8, 0, 8, 0) }
            this.text = "Affirmation ${container.childCount + 1}"   // 👈 IMPORTANT: 'this.text'
            textSize = 18f
            setTextColor(Color.parseColor("#333333"))
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }

        // 2) Bouton micro
        val micBtn = ImageButton(this).apply {
            setImageResource(R.drawable.svg_bouton_micro)
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f
            ).apply { setMargins(8, 0, 8, 0) }
            setBackgroundColor(Color.TRANSPARENT)
            contentDescription = "Maintiens pour enregistrer"
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        micBtn.setOnTouchListener { v, event ->
            val index = container.indexOfChild(row)
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    micBtn.setImageResource(R.drawable.svgmicrojauncetransparent) // pressed
                    startRecording(index)
                    v.isPressed = true
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    micBtn.setImageResource(R.drawable.svg_bouton_micro) // back to normal
                    stopRecording()
                    v.isPressed = false
                    if (event.actionMasked == android.view.MotionEvent.ACTION_UP) v.performClick()
                    true
                }
                else -> false
            }
        }

        // 3) Bouton supprimer
        val deleteBtn = ImageButton(this).apply {
            setImageResource(R.drawable.croix_jaune_fusion)
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f
            ).apply { setMargins(8, 0, 0, 0) }
            setBackgroundColor(Color.TRANSPARENT)
            contentDescription = "Supprimer cet enregistrement"
        }
        deleteBtn.setOnClickListener {
            val index = container.indexOfChild(row)
            if (isRecording && currentRecordingIndex == index) stopRecording(force = true)
            generateFiles.getOrNull(index)?.let { p -> if (p.isNotBlank()) try { File(p).delete() } catch (_: Exception) {} }
            if (index in userText.indices) userText.removeAt(index)
            if (index in 0 until generateFiles.size) generateFiles.removeAt(index)
            row.findViewById<PlayerView>(R.id.player_view_inline)?.player?.release() // ⬅️ release
            container.removeView(row)
            updateAffirmationLabels()
        }

        row.addView(label)
        row.addView(micBtn)
        row.addView(deleteBtn)
        container.addView(row)

        updateAffirmationLabels()
    }

    private fun updateAffirmationLabels() {
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i) as LinearLayout
            val label = row.getChildAt(0) as TextView
            label.text = "Affirmation ${i + 1}"
        }
    }


    /**
     * Génère un fichier audio TTS pour un texte spécifique sans ajouter le nom.
     */
    private fun textToSpeech(text: String, index: Int, voiceId: String) {
        val apiKey = BuildConfig.ELEVENLABS_API_KEY
        val client = OkHttpClient()
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (audioDir.exists()) {
            audioDir.listFiles()?.forEach { it.delete() }
        } else {
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
