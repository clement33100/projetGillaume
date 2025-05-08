package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.navigation.NavigationView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class step3Music : Base() {  // Hérite de Base au lieu de AppCompatActivity

    // UI Elements
    private lateinit var uploadButton: Button
    private lateinit var dynamicButtonContainer: LinearLayout
    private var savedFilePath: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private val fileList = mutableListOf<Pair<String, String>>()
    private var currentFilePath: String? = null

    private var songChoose: String? = null

    private lateinit var btn_ChoixEpic1: Button
    private lateinit var btn_ChoixEpic2: Button
    private lateinit var btn_ChoixEpic3: Button

    private lateinit var btn_ChoixFrequenceVibratoire1: Button
    private lateinit var btn_ChoixFrequenceVibratoire2: Button
    private lateinit var btn_ChoixFrequenceVibratoire3: Button

    private lateinit var btn_ok: Button

    private lateinit var scrollview_Epic: ScrollView
    private lateinit var scrollview_FrequenceVibratoire: ScrollView
//
//    private lateinit var epicMusic1: ImageButton
//    private lateinit var epicMusic2: ImageButton
//    private lateinit var epicMusic3: ImageButton

    private lateinit var FrequenceVibratoire1: ImageButton
    private lateinit var FrequenceVibratoire2: ImageButton
    private lateinit var FrequenceVibratoire3: ImageButton

    private lateinit var btn_silence: Button // Ajout du bouton Silence

    // Variable pour stocker le chemin du fichier silencieux
    private var savedFilePathSilence: String? = null

    // Activity Result Launcher pour la sélection de fichiers audio
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val audioFileName = getFileName(it)
                savedFilePath = saveFileToInternalStorage(it, audioFileName.toString())
                fileList.add(Pair(audioFileName.toString(), savedFilePath.toString()))
                createDynamicButton(audioFileName.toString(), savedFilePath.toString())
                Toast.makeText(this, "Selected: $audioFileName", Toast.LENGTH_SHORT).show()
                // Ici, vous pouvez gérer le fichier MP3 (téléchargement, lecture, etc.)
            }
        }

    override fun getLayoutId(): Int {
        return R.layout.activity_step3_music  // Fournit le layout spécifique à cette activité
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Configuration des Insets UI
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

        // Initialisation des éléments UI existants
        uploadButton = findViewById(R.id.uploadButton)
        dynamicButtonContainer = findViewById(R.id.dynamicButtonContainer)
        uploadButton.setOnClickListener {
            openAudioFilePicker()
        }

        btn_ok = findViewById(R.id.btn_okmusic)
        btn_silence = findViewById(R.id.btn_silence) // Initialisation du bouton Silence

//        scrollview_Epic = findViewById<ScrollView>(R.id.scrollViewEpic)
          scrollview_FrequenceVibratoire = findViewById<ScrollView>(R.id.scrollViewFrequenceVibratoire)
//        epicMusic1 = findViewById<ImageButton>(R.id.listenepic1)
//        epicMusic2 = findViewById<ImageButton>(R.id.listenepic2)
//        epicMusic3 = findViewById<ImageButton>(R.id.listenepic3)

        FrequenceVibratoire1 = findViewById<ImageButton>(R.id.listenfrequence1)
        FrequenceVibratoire2 = findViewById<ImageButton>(R.id.listenfrequence2)
        FrequenceVibratoire3 = findViewById<ImageButton>(R.id.listenfrequence3)
//
//        btn_ChoixEpic1 = findViewById<Button>(R.id.epic1)
//        btn_ChoixEpic2 = findViewById<Button>(R.id.epic2)
//        btn_ChoixEpic3 = findViewById<Button>(R.id.epic3)

        btn_ChoixFrequenceVibratoire1 = findViewById<Button>(R.id.frequence1)
        btn_ChoixFrequenceVibratoire2 = findViewById<Button>(R.id.frequence2)
        btn_ChoixFrequenceVibratoire3 = findViewById<Button>(R.id.frequence3)

//        val ButtonEpic = findViewById<Button>(R.id.btn_epic)
        val ButtonVib = findViewById<Button>(R.id.btn_frequence)
        val curentVoice = intent.getStringExtra("curentVoice")
        val userTexts = intent.getStringArrayListExtra("userTexts")

        val userTextsSplit = intent.getStringArrayListExtra("userTextsSplit")
        val nom = intent.getStringExtra("nom")
        val curentAPIKey = intent.getStringExtra("curentAPIKey")

        val intention = intent.getBooleanExtra("intention", false)

        if (userTextsSplit != null) {
            for (text in userTextsSplit) {
                Log.d("test12345", "Received text: $text")
            }
        } else {
            Log.d("test12345", "No texts received")
        }
        if (userTexts != null) {
            for (text in userTexts) {
                Log.d("test123", "Received text: $text")
            }
        } else {
            Log.d("test123", "No texts received")
        }

        // Copier les fichiers audio dans le stockage interne
        val puissanceinterieure = "puissanceinterieure.mp3"  // Nom du fichier dans le stockage interne
        //val savedFilePathPuissanceinterieure = copyRawResourceToInternalStorage(R.raw.puissance, puissanceinterieure)

        val renaissance = "renaissance.mp3"
        //val savedFilePathRenaissance = copyRawResourceToInternalStorage(R.raw.renaissance, renaissance)

        val ressourceinfinie = "ressourceinfinie.mp3"
        //val savedFilePathRessourceinfinie = copyRawResourceToInternalStorage(R.raw.ressourceinfinie, ressourceinfinie)

        val expansion = "abondance.mp3"
        val savedFilePathExpansion = copyRawResourceToInternalStorage(R.raw.expansion, expansion)

        val ondeteta = "focus.mp3"
        val savedFilePathOndeTeta = copyRawResourceToInternalStorage(R.raw.ondeteata, ondeteta)

        val voyageinterieur = "intuition.mp3"
        val savedFilePathVoyageInterieur = copyRawResourceToInternalStorage(R.raw.voyageinterieur, voyageinterieur)

        // Copier le fichier silence dans le stockage interne
        val silence = "silence.mp3"
        savedFilePathSilence = copyRawResourceToInternalStorage(R.raw.silence, silence)

        // Configuration des boutons d'écoute pour les musiques Epic
//        epicMusic1.setOnClickListener{
//            if (savedFilePathPuissanceinterieure != null) {
//                playAudio(savedFilePathPuissanceinterieure)
//            } else {
//                //Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
//            }
//        }
//        epicMusic2.setOnClickListener{
//            if (savedFilePathRenaissance != null) {
//                playAudio(savedFilePathRenaissance)
//            } else {
//                //Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        epicMusic3.setOnClickListener{
//            if (savedFilePathRessourceinfinie != null) {
//                playAudio(savedFilePathRessourceinfinie)
//            } else {
//                //Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
//            }
//        }

        // Configuration des boutons d'écoute pour les fréquences vibratoires
        FrequenceVibratoire1.setOnClickListener{
            if (savedFilePathExpansion != null) {
                playAudio(savedFilePathExpansion)
            } else {
                //Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }
        FrequenceVibratoire2.setOnClickListener{
            if (savedFilePathOndeTeta != null) {
                playAudio(savedFilePathOndeTeta)
            } else {
                //Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }
        FrequenceVibratoire3.setOnClickListener{
            if (savedFilePathVoyageInterieur != null) {
                playAudio(savedFilePathVoyageInterieur)
            } else {
                //Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configuration des boutons de choix pour les musiques Epic
//        btn_ChoixEpic1.setOnClickListener {
//            setTextInfo(btn_ChoixEpic1.text.toString(), savedFilePathPuissanceinterieure)
//            setViewVisibilityGone(scrollview_Epic)
//            setViewVisibilityGone(scrollview_FrequenceVibratoire)
//        }
//        btn_ChoixEpic2.setOnClickListener {
//            setTextInfo(btn_ChoixEpic2.text.toString(), savedFilePathRenaissance)
//            setViewVisibilityGone(scrollview_Epic)
//            setViewVisibilityGone(scrollview_FrequenceVibratoire)
//        }
//        btn_ChoixEpic3.setOnClickListener {
//            setTextInfo(btn_ChoixEpic3.text.toString(), savedFilePathRessourceinfinie)
//            setViewVisibilityGone(scrollview_Epic)
//            setViewVisibilityGone(scrollview_FrequenceVibratoire)
//        }

        // Configuration des boutons de choix pour les fréquences vibratoires
        btn_ChoixFrequenceVibratoire1.setOnClickListener {
            setTextInfo(btn_ChoixFrequenceVibratoire1.text.toString(), savedFilePathExpansion)
            //setViewVisibilityGone(scrollview_Epic)
            setViewVisibilityGone(scrollview_FrequenceVibratoire)
        }
        btn_ChoixFrequenceVibratoire2.setOnClickListener {
            setTextInfo(btn_ChoixFrequenceVibratoire2.text.toString(), savedFilePathOndeTeta)
            //setViewVisibilityGone(scrollview_Epic)
            setViewVisibilityGone(scrollview_FrequenceVibratoire)
        }
        btn_ChoixFrequenceVibratoire3.setOnClickListener {
            setTextInfo(btn_ChoixFrequenceVibratoire3.text.toString(), savedFilePathVoyageInterieur)
            //setViewVisibilityGone(scrollview_Epic)
            setViewVisibilityGone(scrollview_FrequenceVibratoire)
        }

        // Configuration du bouton Silence
        btn_silence.setOnClickListener {
            if (savedFilePathSilence != null) {
                setTextInfo("Silence", savedFilePathSilence)
                //setViewVisibilityGone(scrollview_Epic)
                setViewVisibilityGone(scrollview_FrequenceVibratoire)
                //Toast.makeText(this, "Silence sélectionné", Toast.LENGTH_SHORT).show()
            } else {
                //Toast.makeText(this, "Échec de la sauvegarde du fichier silencieux.", Toast.LENGTH_SHORT).show()
            }
        }

        btn_ok.setOnClickListener {
            if (userTextsSplit != null) {
                generateTTSFilesForAllTexts(curentAPIKey, userTextsSplit, userTexts)
            }

            if (songChoose == null) {
                //Toast.makeText(this, "Sélectionnez une musique", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, Step4::class.java)
                intent.putExtra("filePaths", songChoose)
                intent.putExtra("curentVoice", curentVoice)
                intent.putStringArrayListExtra("userTexts", userTexts)
                intent.putExtra("intention", intention)

                if (userTextsSplit != null && userTextsSplit.size > 4) {
                    intent.putExtra("curentAPIKey", curentAPIKey)
                    intent.putExtra("nom", nom)
                    intent.putStringArrayListExtra("userTextsSplit", userTextsSplit)
                }

                startActivity(intent)
            }
        }

//        // Initialisation des boutons Epic et Vibration
//        ButtonEpic.setOnClickListener{
//            setViewVisibility(scrollview_Epic)
//        }
        ButtonVib.setOnClickListener {
            setViewVisibility(scrollview_FrequenceVibratoire)
        }
    }

    /**
     * Génère les fichiers TTS pour tous les textes.
     */
    private fun generateTTSFilesForAllTexts(apikey: String?, userTextsSplit: ArrayList<String>?, userTexts: ArrayList<String>?) {
        if (userTextsSplit != null) {
            for (index in 4..7) {  // Exemple : Générer pour les indices 2 et 3
                if (index < userTextsSplit.size) {
                    val text = userTextsSplit[index]
                    if ( apikey != null) {
                        textToSpeech(text, index, apikey, userTexts)
                    }
                }
            }
        }
    }

    /**
     * Ouvre le sélecteur de fichiers audio.
     */
    private fun openAudioFilePicker() {
        getContent.launch("audio/mpeg")
    }

    /**
     * Met à jour l'information de sélection et le texte affiché.
     */
    private fun setTextInfo(text: String, curentSong: String?) {
        val textInfo = findViewById<TextView>(R.id.textView4)
        songChoose = curentSong
        textInfo.text = text
    }

    /**
     * Récupère le nom du fichier à partir de son URI.
     */
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    result = cursor.getString(nameIndex)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    /**
     * Sauvegarde un fichier dans le stockage interne.
     */
    private fun saveFileToInternalStorage(uri: Uri, fileName: String): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // 4KB buffer
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }

        return file.absolutePath
    }

    /**
     * Copie une ressource brute dans le stockage interne et retourne son chemin.
     */
    private fun copyRawResourceToInternalStorage(resourceId: Int, fileName: String): String? {
        val uri = Uri.parse("android.resource://${packageName}/$resourceId")
        return try {
            saveFileToInternalStorage(uri, fileName)
        } catch (e: Exception) {
            Log.e("step3Music", "Error copying resource $fileName: ${e.message}")
            null
        }
    }

    /**
     * Cache une ScrollView si elle est visible.
     */
    private fun setViewVisibilityGone(scrollview: ScrollView) {
        if (scrollview.visibility == View.VISIBLE) {
            scrollview.visibility = View.GONE
        }
    }

    /**
     * Crée dynamiquement un bouton pour un fichier audio sélectionné.
     */
    private fun createDynamicButton(fileName: String, filePath: String) {
        // Supprimer tout ce qui était dans le container (pour ne garder qu’un seul élément)
        dynamicButtonContainer.removeAllViews()

        // Charger la disposition personnalisée
        val inflater = LayoutInflater.from(this)
        val rowView = inflater.inflate(R.layout.item_music, dynamicButtonContainer, false)

        // Récupérer les vues du layout personnalisé
        val btnMusicText = rowView.findViewById<Button>(R.id.btn_music_text)
        val imgBtnPlay = rowView.findViewById<ImageButton>(R.id.img_btn_play)

        // Définir le texte du bouton avec le nom du fichier
        btnMusicText.text = fileName

        // Définir les actions
        btnMusicText.setOnClickListener {
            //Toast.makeText(this, "Tu as sélectionné $fileName", Toast.LENGTH_SHORT).show()
            setTextInfo(fileName, filePath)
        }

        imgBtnPlay.setOnClickListener {
            playAudio(filePath)
        }

        // Ajouter la vue
        dynamicButtonContainer.addView(rowView)

        // Forcer le scroll en bas pour que l'utilisateur voit la musique ajoutée
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }

        // Mettre à jour la musique sélectionnée
        setTextInfo(fileName, filePath)
    }


    /**
     * Joue un fichier audio à partir de son chemin.
     */
    private fun playAudio(filePath: String) {
        if (mediaPlayer == null) {
            // Initialiser et démarrer la lecture
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
            currentFilePath = filePath
            //Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
        } else {
            if (mediaPlayer?.isPlaying == true && currentFilePath == filePath) {
                // Arrêter la lecture
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                currentFilePath = null
                //Toast.makeText(this, "Stopping audio", Toast.LENGTH_SHORT).show()
            } else {
                // Changer de fichier audio ou redémarrer la lecture
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(filePath)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                currentFilePath = filePath
                //Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Change la visibilité d'une ScrollView.
     */
    private fun setViewVisibility(scrollview: ScrollView) {
        if (scrollview.visibility == View.GONE) {
            scrollview.visibility = View.VISIBLE
        } else {
            scrollview.visibility = View.GONE
        }
    }

    /**
     * Texte à la synthèse vocale via l'API ElevenLabs.
     */
    private fun textToSpeech(text: String, index: Int, voiceId: String, userTexts: ArrayList<String>?) {
        val apiKey = "sk_1e85a97e6cdd33e449f8578f3fa7152594bdab061b0649b7" // Remplacez avec votre clé API

        val client = OkHttpClient()
        val basePath = filesDir.absolutePath + "/audio/"
        val audioDir = File(basePath)
        if (!audioDir.exists()) {
            audioDir.mkdirs()  // Créer le dossier si nécessaire
        }

        // Générer un fichier avec un nom unique basé sur l'index
        val generatedFilePath = "$basePath/voice_$index.mp3"
        userTexts?.add(generatedFilePath)

        val fullText = "$text."

        // Créer le corps de la requête en JSON
        val bodyJson = JSONObject().apply {
            put("text", fullText)
            put("model_id", "eleven_turbo_v2_5") // Utiliser un modèle multilingue
            put("language_code", "fr") // Définir le code langue en français
            put("voice_settings", JSONObject().apply {
                put("stability", 0.5)
                put("similarity_boost", 0.75)
                // Vous pouvez ajuster ces valeurs pour affiner l'accent
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
                Log.e("step3Music", "Erreur lors de l'appel API : ${e.message}")
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

                        Log.d("step3Music", "Fichier audio sauvegardé à : ${audioFile.absolutePath}")

                        // Notification de succès
                        runOnUiThread {
                            //Toast.makeText(this@step3Music, "Fichier audio généré pour l'index $index", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: IOException) {
                        Log.e("step3Music", "Erreur lors de la sauvegarde de l'audio : ${e.message}")
                    }

                } else {
                    Log.d("step3Music", "Le corps de la réponse est null")
                }
            }
        })
    }



    override fun onPause() {
        super.onPause()
        stopAudio()
    }

    /**
     * Arrête la lecture audio si en cours.
     */
    private fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        currentFilePath = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
