package com.example.myapplicationv2

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class ExempleMesAffirmations : AppCompatActivity() {

    private lateinit var btn_Ecouter: Button
    private lateinit var btn_Telecharger: Button
    private lateinit var btn_Partager : Button

    private var mediaPlayer: MediaPlayer? = null

    //a faire télécharger écouter partager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        setContentView(R.layout.activity_exemple_mes_affirmations)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_Telecharger=findViewById<Button>(R.id.btn_dl_mesAffirmation)

        btn_Ecouter = findViewById<Button>(R.id.btn_Ecouter_exempleaffirmation)

        btn_Ecouter.setOnClickListener {
            playAudioFromRaw(R.raw.exemplemesaffirmations)

        }

        /*btn_Telecharger.setOnClickListener{
            copyMusicToStorage(R.raw.telechargermesaffirmations)

        }*/


    }

/*
    private fun copyMusicToStorage(audioResId: Int) {
        val inputStream: InputStream = resources.openRawResource(audioResId) // Remplacez par le nom de votre fichier
        val musicDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: return
        if (!musicDir.exists()) {
            musicDir.mkdirs()
        }
        val file = File(musicDir, "your_music_file.mp3") // Nom du fichier de destination
        Toast.makeText(this, "Download: $musicDir", Toast.LENGTH_SHORT).show()

        try {
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var len: Int

            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }

            outputStream.close()
            inputStream.close()
            Log.d("CopyMusic", "File copied: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("CopyMusic", "Error: ${e.message}")
        }
    }*/



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
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }



}