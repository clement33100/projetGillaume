package com.example.myapplicationv2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplicationv2.R.id.uploadButton
import kotlin.math.log
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class step3Music : AppCompatActivity() {


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

    private lateinit var epicMusic1: ImageButton
    private lateinit var epicMusic2: ImageButton
    private lateinit var epicMusic3: ImageButton

    private lateinit var FrequenceVibratoire1: ImageButton
    private lateinit var FrequenceVibratoire2: ImageButton
    private lateinit var FrequenceVibratoire3: ImageButton





    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val audioFileName = getFileName(it)
                savedFilePath = saveFileToInternalStorage(it, audioFileName.toString())
                fileList.add(Pair(audioFileName.toString(), savedFilePath.toString()))
                createDynamicButton(audioFileName.toString(), savedFilePath.toString())
                Toast.makeText(this, "Selected: $audioFileName", Toast.LENGTH_SHORT).show()
                // Here you can handle the MP3 file (e.g., upload it to a server, play it, etc.)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_step3_music)

        val ButtonEpic = findViewById<Button>(R.id.btn_epic)
        val ButtonVib = findViewById<Button>(R.id.btn_frequence)
        val curentVoice = intent.getStringExtra("curentVoice")
        val userTexts = intent.getStringArrayListExtra("userTexts")

        val userTextsSplit = intent.getStringArrayListExtra("userTextsSplit")
        val nom = intent.getStringExtra("nom")
        val curentAPIKey = intent.getStringExtra("curentAPIKey")

        if (userTextsSplit != null) {
            for (text in userTextsSplit) {
                Log.d("test12345", "Received text: $text")
            }
        } else {
            Log.d("test12345", "No texts received")
        }

        scrollview_Epic = findViewById<ScrollView>(R.id.scrollViewEpic)
        scrollview_FrequenceVibratoire = findViewById<ScrollView>(R.id.scrollViewFrequenceVibratoire)
        epicMusic1 = findViewById<ImageButton>(R.id.listenepic1)
        epicMusic2 = findViewById<ImageButton>(R.id.listenepic2)
        epicMusic3 = findViewById<ImageButton>(R.id.listenepic3)

        FrequenceVibratoire1 = findViewById<ImageButton>(R.id.listenfrequence1)
        FrequenceVibratoire2 = findViewById<ImageButton>(R.id.listenfrequence2)
        FrequenceVibratoire3 = findViewById<ImageButton>(R.id.listenfrequence3)

        btn_ChoixEpic1 = findViewById<Button>(R.id.epic1)
        btn_ChoixEpic2 = findViewById<Button>(R.id.epic2)
        btn_ChoixEpic3 = findViewById<Button>(R.id.epic3)

        btn_ChoixFrequenceVibratoire1 = findViewById<Button>(R.id.frequence1)
        btn_ChoixFrequenceVibratoire2 = findViewById<Button>(R.id.frequence2)
        btn_ChoixFrequenceVibratoire3 = findViewById<Button>(R.id.frequence3)

        btn_ok = findViewById<Button>(R.id.btn_okmusic)

        /*ImageButton.setOnClickListener {
            if (fileList.isNotEmpty()) {
                val intent = Intent(this, Step4::class.java)
                val filePaths = fileList.map { it.second }.toTypedArray()
                intent.putExtra("filePaths", filePaths)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No files to pass", Toast.LENGTH_SHORT).show()
            }
        }*/


        ButtonEpic.setOnClickListener{
            setViewVisibility(scrollview_Epic)


        }
        ButtonVib.setOnClickListener {
            setViewVisibility(scrollview_FrequenceVibratoire)

        }

        val puissanceinterieure = "puissanceinterieure.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathPuissanceinterieure = copyRawResourceToInternalStorage(R.raw.puissanceinterieure, puissanceinterieure)

        val renaissance = "renaissance.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathRenaissance = copyRawResourceToInternalStorage(R.raw.renaissance, renaissance)

        val ressourceinfinie = "ressourceinfinie.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathRessourceinfinie = copyRawResourceToInternalStorage(R.raw.ressourceinfinie, ressourceinfinie)

        val abondance = "abondance.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathAbondance = copyRawResourceToInternalStorage(R.raw.abondance, abondance)

        val focus = "focus.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathFocus = copyRawResourceToInternalStorage(R.raw.focus, focus)

        val intuition = "intuition.mp3"  // Le nom que vous souhaitez donner au fichier dans le stockage interne
        val savedFilePathIntuition = copyRawResourceToInternalStorage(R.raw.intuition, intuition)

        epicMusic1.setOnClickListener{
            //playAudioFromRaw(R.raw.epicinstantcrush)
            if (savedFilePathPuissanceinterieure != null) {
                // Jouer le fichier audio en utilisant la fonction playAudio
                playAudio(savedFilePathPuissanceinterieure)
            } else {
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }
        epicMusic2.setOnClickListener{
            if (savedFilePathRenaissance != null) {
                // Jouer le fichier audio en utilisant la fonction playAudio
                playAudio(savedFilePathRenaissance)
            } else {
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }

        epicMusic3.setOnClickListener{
            if (savedFilePathRessourceinfinie != null) {
                // Jouer le fichier audio en utilisant la fonction playAudio
                playAudio(savedFilePathRessourceinfinie)
            } else {
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }

        FrequenceVibratoire1.setOnClickListener{
            if (savedFilePathAbondance != null) {
                // Jouer le fichier audio en utilisant la fonction playAudio
                playAudio(savedFilePathAbondance)
            } else {
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }
        FrequenceVibratoire2.setOnClickListener{
            if (savedFilePathFocus != null) {
                // Jouer le fichier audio en utilisant la fonction playAudio
                playAudio(savedFilePathFocus)
            } else {
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }
        FrequenceVibratoire3.setOnClickListener{
            if (savedFilePathIntuition != null) {
                // Jouer le fichier audio en utilisant la fonction playAudio
                playAudio(savedFilePathIntuition)
            } else {
                Toast.makeText(this, "Failed to save the audio file.", Toast.LENGTH_SHORT).show()
            }
        }

        btn_ChoixEpic1.setOnClickListener {
            // Jouer le fichier audio en utilisant la fonction playAudio
            setTextInfo(btn_ChoixEpic1.text.toString(),savedFilePathPuissanceinterieure)



        }
        btn_ChoixEpic2.setOnClickListener {
            setTextInfo(btn_ChoixEpic2.text.toString(),savedFilePathRenaissance)


        }
        btn_ChoixEpic3.setOnClickListener {
            setTextInfo(btn_ChoixEpic3.text.toString(),savedFilePathRessourceinfinie)


        }
        btn_ChoixFrequenceVibratoire1.setOnClickListener {
            setTextInfo(btn_ChoixFrequenceVibratoire1.text.toString(),savedFilePathAbondance)


        }
        btn_ChoixFrequenceVibratoire2.setOnClickListener {

            setTextInfo(btn_ChoixFrequenceVibratoire2.text.toString(),savedFilePathFocus)

        }
        btn_ChoixFrequenceVibratoire3.setOnClickListener {

            setTextInfo(btn_ChoixFrequenceVibratoire3.text.toString(),savedFilePathIntuition)

        }


        btn_ok.setOnClickListener {


            if (userTextsSplit != null) {

                generateTTSFilesForAllTexts(nom, curentAPIKey, userTextsSplit, userTexts)


            }

            if (songChoose == null) {
                Toast.makeText(this, "Selectionner une musique", Toast.LENGTH_SHORT).show()

            } else {

                val intent = Intent(this, Step4::class.java)
                intent.putExtra("filePaths", songChoose)
                intent.putExtra("curentVoice", curentVoice)
                intent.putStringArrayListExtra("userTexts", userTexts)

                if (userTextsSplit != null) {
                    if(userTextsSplit.size>4){
                        intent.putExtra("curentAPIKey", curentAPIKey)
                        intent.putExtra("nom", nom)
                        intent.putStringArrayListExtra("userTextsSplit", userTextsSplit)
                    }
                }



                startActivity(intent)

            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,-
                systemBars.bottom
            )
            insets
        }

        dynamicButtonContainer = findViewById(R.id.dynamicButtonContainer)
        uploadButton = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            openAudioFilePicker()
        }

        /*anotherButton.setOnClickListener {
            // Ajouter une action pour un autre bouton si nécessaire
            //Toast.makeText(this, anotherButton.text, Toast.LENGTH_SHORT).show()
            savedFilePath?.let {
                playAudio(it)
            } ?: Toast.makeText(this, "No audio file selected", Toast.LENGTH_SHORT).show()
        }*/
    }

    private fun generateTTSFilesForAllTexts(nom: String?, apikey: String?,userTextsSplit: ArrayList<String>?,userTexts: ArrayList<String>?) {
        if (userTextsSplit != null) {
            for (index in 2..3) {
                if (index < userTextsSplit.size) {
                    val text = userTextsSplit[index]
                    if (nom != null && apikey != null) {
                        textToSpeech(text, nom, index, apikey,userTexts)
                    }
                }
            }
        }
    }

    private fun openAudioFilePicker() {
        getContent.launch("audio/mpeg")
    }

    private fun setTextInfo(text : String,curentSong: String?){
        val textInfo = findViewById<TextView>(R.id.textView4)
        songChoose = curentSong
        textInfo.setText("Ton choix : "+text)
    }


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


    /*private fun createDynamicButton(fileName: String, filePath: String) {

        val button = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
            text = "Play: $fileName"
        }

        button.setOnClickListener {
            playAudio(filePath)
        }

        dynamicButtonContainer.addView(button)
    }*/


    private fun copyRawResourceToInternalStorage(resourceId: Int, fileName: String): String? {
        val uri = Uri.parse("android.resource://${packageName}/$resourceId")
        return saveFileToInternalStorage(uri, fileName)
    }

    private fun createDynamicButton(fileName: String, filePath: String) {
        // Charger la disposition personnalisée
        val inflater = LayoutInflater.from(this)
        val rowView = inflater.inflate(R.layout.item_music, dynamicButtonContainer, false)

        // Récupérer les vues du layout personnalisé
        val btnMusicText = rowView.findViewById<Button>(R.id.btn_music_text)
        val imgBtnPlay = rowView.findViewById<ImageButton>(R.id.img_btn_play)

        // Définir le texte du bouton avec le nom du fichier
        btnMusicText.text = fileName

        // Définir l'action de clic sur le bouton de texte si besoin (par exemple pour afficher les paroles)
        btnMusicText.setOnClickListener {
            // Action lorsque le bouton texte est cliqué
            Toast.makeText(this, "You clicked on $fileName", Toast.LENGTH_SHORT).show()
            setTextInfo(btnMusicText.text.toString(),filePath)
        }

        // Définir l'action de clic sur l'ImageButton pour jouer la musique
        imgBtnPlay.setOnClickListener {
            playAudio(filePath)
        }

        // Ajouter la vue à votre conteneur
        dynamicButtonContainer.addView(rowView)
    }


    private fun playAudioFromRaw(audioResId: Int) {
        if (mediaPlayer == null) {
            // Initialize and start playback
            mediaPlayer = MediaPlayer.create(this, audioResId)
            mediaPlayer?.start()
            Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
        } else {
            if (mediaPlayer?.isPlaying == true) {
                // Stop playback
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer = null
                Toast.makeText(this, "Stopping audio", Toast.LENGTH_SHORT).show()
            } else {
                // Restart playback
                mediaPlayer = MediaPlayer.create(this, audioResId)
                mediaPlayer?.start()
                Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playAudio(filePath: String) {
        if (mediaPlayer == null) {
            // Initialize and start playback
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
            currentFilePath = filePath
            Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
        } else {
            if (mediaPlayer?.isPlaying == true && currentFilePath == filePath) {
                // Stop playback
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                currentFilePath = null
                Toast.makeText(this, "Stopping audio", Toast.LENGTH_SHORT).show()
            } else {
                // Switch to new audio file or restart current one
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(filePath)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                currentFilePath = filePath
                Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setViewVisibility(scrollview : ScrollView){

        if(scrollview.visibility== View.GONE){
            scrollview.visibility = View.VISIBLE
        }
        else {
            scrollview.visibility = View.GONE
        }
    }

    private fun textToSpeech(text: String,nom:String, index: Int, voiceId: String,userTexts: ArrayList<String>?) {
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
        userTexts?.add(generatedFilePath)

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
                            Toast.makeText(this@step3Music, "Fichier audio généré pour l'index $index", Toast.LENGTH_SHORT).show()
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



    override fun onPause() {
        super.onPause()
        stopAudio()
    }

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
