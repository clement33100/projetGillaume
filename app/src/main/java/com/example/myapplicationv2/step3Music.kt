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
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class step3Music : AppCompatActivity() {


    private lateinit var uploadButton: Button
    private lateinit var dynamicButtonContainer: LinearLayout
    private var savedFilePath: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private val fileList = mutableListOf<Pair<String, String>>()
    private var currentFilePath: String? = null


    private lateinit var btn_ChoixEpic1: Button
    private lateinit var btn_ChoixEpic2: Button

    private lateinit var btn_ChoixFrequenceVibratoire1: Button
    private lateinit var btn_ChoixFrequenceVibratoire2: Button

    private lateinit var btn_ok: Button


    private lateinit var scrollview_Epic: ScrollView
    private lateinit var scrollview_FrequenceVibratoire: ScrollView

    private lateinit var epicMusic1: ImageButton
    private lateinit var epicMusic2: ImageButton

    private lateinit var FrequenceVibratoire1: ImageButton
    private lateinit var FrequenceVibratoire2: ImageButton

    private var curentVoice: Int?=null




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

        val ImageButton = findViewById<ImageButton>(R.id.imageButtonLFirstStep)
        val ButtonEpic = findViewById<Button>(R.id.btn_epic)
        val ButtonVib = findViewById<Button>(R.id.btn_frequence)


        scrollview_Epic = findViewById<ScrollView>(R.id.scrollViewEpic)
        scrollview_FrequenceVibratoire = findViewById<ScrollView>(R.id.scrollViewFrequenceVibratoire)
        epicMusic1 = findViewById<ImageButton>(R.id.listenepic1)
        epicMusic2 = findViewById<ImageButton>(R.id.listenepic2)
        FrequenceVibratoire1 = findViewById<ImageButton>(R.id.listenfrequence1)
        FrequenceVibratoire2 = findViewById<ImageButton>(R.id.listenfrequence2)

        btn_ChoixEpic1 = findViewById<Button>(R.id.epic1)
        btn_ChoixEpic2 = findViewById<Button>(R.id.epic2)

        btn_ChoixFrequenceVibratoire1 = findViewById<Button>(R.id.frequence1)
        btn_ChoixFrequenceVibratoire2 = findViewById<Button>(R.id.frequence2)

        btn_ok = findViewById<Button>(R.id.btn_okmusic)
        ImageButton.setOnClickListener {
            val intent = Intent(this, Step4::class.java)
            val filePaths = fileList.map { it.second }.toTypedArray()
            intent.putExtra("filePaths", filePaths)

            startActivity(intent)

        }
        ButtonEpic.setOnClickListener{
            setViewVisibility(scrollview_Epic)


        }
        ButtonVib.setOnClickListener {
            setViewVisibility(scrollview_FrequenceVibratoire)

        }

        epicMusic1.setOnClickListener{
            playAudioFromRaw(R.raw.epicinstantcrush)
        }
        epicMusic2.setOnClickListener{
            playAudioFromRaw(R.raw.epictobuildhome)
        }
        FrequenceVibratoire1.setOnClickListener{
            playAudioFromRaw(R.raw.viblonely)
        }
        FrequenceVibratoire2.setOnClickListener{
            playAudioFromRaw(R.raw.vibgoodbye)
        }

        btn_ChoixEpic1.setOnClickListener {

            setTextInfo(btn_ChoixEpic1.text.toString(),R.raw.epicinstantcrush)

        }
        btn_ChoixEpic2.setOnClickListener {
            setTextInfo(btn_ChoixEpic2.text.toString(),R.raw.epictobuildhome)


        }
        btn_ChoixFrequenceVibratoire1.setOnClickListener {
            setTextInfo(btn_ChoixFrequenceVibratoire1.text.toString(),R.raw.viblonely)


        }
        btn_ChoixFrequenceVibratoire2.setOnClickListener {

            setTextInfo(btn_ChoixFrequenceVibratoire2.text.toString(),R.raw.vibgoodbye)

        }
        btn_ok.setOnClickListener {
            if (curentVoice == null) {
                Toast.makeText(this, "Selectionner une voix", Toast.LENGTH_SHORT).show()

            } else {

                val intent = Intent(this, Step2::class.java)
                intent.putExtra("curentVoice", curentVoice)

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

    private fun openAudioFilePicker() {
        getContent.launch("audio/mpeg")
    }
    private fun setTextInfo(text : String,audioResId: Int){
        val textInfo = findViewById<TextView>(R.id.textView4)
        curentVoice = audioResId
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


    private fun createDynamicButton(fileName: String, filePath: String) {
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}
