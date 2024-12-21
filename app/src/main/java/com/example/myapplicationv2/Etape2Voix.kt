package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import kotlin.math.log

class Etape2Voix : AppCompatActivity() {
    private lateinit var btn_VoiceFemme: Button
    private lateinit var btn_VoiceHomme: Button

    private lateinit var btn_ChoixVoiceHomme1: Button
    private lateinit var btn_ChoixVoiceHomme2: Button
    private lateinit var btn_ChoixVoiceHomme3: Button
    private lateinit var btn_ChoixVoiceHomme4: Button

    private lateinit var btn_ChoixVoiceFemme1: Button
    private lateinit var btn_ChoixVoiceFemme2: Button
    private lateinit var btn_ChoixVoiceFemme3: Button
    private lateinit var btn_ChoixVoiceFemme4: Button

    private lateinit var btn_ok: Button
    private var currentFilePath: String? = null


    private lateinit var scrollview_FemmeVoice: ScrollView
    private lateinit var scrollview_HommeVoice: ScrollView
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var voicefemme1: ImageButton
    private lateinit var voicefemme2: ImageButton
    private lateinit var voicefemme3: ImageButton
    private lateinit var voicefemme4: ImageButton

    private lateinit var voicehomme1: ImageButton
    private lateinit var voicehomme2: ImageButton
    private lateinit var voicehomme3: ImageButton
    private lateinit var voicehomme4: ImageButton

    private var curentVoice: String?=null
    private var currentApiKey: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_etape2_voix)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btn_VoiceFemme = findViewById<Button>(R.id.btn_voix_femme)
        btn_VoiceHomme = findViewById<Button>(R.id.btn_voix_homme)
        scrollview_FemmeVoice = findViewById<ScrollView>(R.id.scrollViewFemmeVoice)
        scrollview_HommeVoice = findViewById<ScrollView>(R.id.scrollViewHommeVoice)
        voicefemme1 = findViewById<ImageButton>(R.id.listenFemme1)
        voicefemme2 = findViewById<ImageButton>(R.id.listenFemme2)
        voicefemme3 = findViewById<ImageButton>(R.id.listenFemme3)
        voicefemme4 = findViewById<ImageButton>(R.id.listenFemme4)
        voicehomme1 = findViewById<ImageButton>(R.id.listenHomme1)
        voicehomme2 = findViewById<ImageButton>(R.id.listenHomme2)
        voicehomme3 = findViewById<ImageButton>(R.id.listenHomme3)
        voicehomme4 = findViewById<ImageButton>(R.id.listenHomme4)

        btn_ChoixVoiceFemme1 = findViewById<Button>(R.id.voixFemme1)
        btn_ChoixVoiceFemme2 = findViewById<Button>(R.id.voixFemme2)
        btn_ChoixVoiceFemme3 = findViewById<Button>(R.id.voixFemme3)
        btn_ChoixVoiceFemme4 = findViewById<Button>(R.id.voixFemme4)

        btn_ChoixVoiceHomme1 = findViewById<Button>(R.id.voixHomme1)
        btn_ChoixVoiceHomme2 = findViewById<Button>(R.id.voixHomme2)
        btn_ChoixVoiceHomme3 = findViewById<Button>(R.id.voixHomme3)
        btn_ChoixVoiceHomme4 = findViewById<Button>(R.id.voixHomme4)

        btn_ok = findViewById<Button>(R.id.btn_ok_step2)

        val nom = intent.getStringExtra("nom")
        Log.i("TAG", "onCreate: "+nom.toString())
        btn_VoiceFemme.setOnClickListener {
           setViewVisibility(scrollview_FemmeVoice)
        }

        btn_VoiceHomme.setOnClickListener {
            setViewVisibility(scrollview_HommeVoice)
        }

        val basePath = filesDir.absolutePath + "/audio/"

        Log.i("TEST123", "path : "+basePath)


        voicefemme1.setOnClickListener{
            playAudioFromRaw(R.raw.voicefemme1bonjour)
        }
        voicefemme2.setOnClickListener{
            playAudioFromRaw(R.raw.voicefemme2bonjour)
        }
        voicefemme3.setOnClickListener{
            playAudioFromRaw(R.raw.voicefemme3bonjour)
        }
        voicefemme4.setOnClickListener{
            playAudioFromRaw(R.raw.voicefemme4bonjour)
        }
        voicehomme1.setOnClickListener{
            playAudioFromRaw(R.raw.voicehomme1bonjour)
        }
        voicehomme2.setOnClickListener{
            playAudioFromRaw(R.raw.voicehomme2bonjour)
        }
        voicehomme3.setOnClickListener{
            playAudioFromRaw(R.raw.voicehomme3bonjour)
        }
        voicehomme4.setOnClickListener{
            playAudioFromRaw(R.raw.voicehomme4bonjour)
        }

        btn_ChoixVoiceFemme1.setOnClickListener {

            setTextInfo(btn_ChoixVoiceFemme1.text.toString(),"$basePath/voiceFemale1.mp3")
            currentApiKey="PLF9iqQhzBVpMoW6O9ja"
            setViewVisibilityGone(scrollview_FemmeVoice)
            setViewVisibilityGone(scrollview_HommeVoice)


        }
        btn_ChoixVoiceFemme2.setOnClickListener {
            setTextInfo(btn_ChoixVoiceFemme2.text.toString(),"$basePath/voiceFemale2.mp3")
            currentApiKey="ECwDrhqw5hpzmlbcIzto"
            setViewVisibilityGone(scrollview_FemmeVoice)
            setViewVisibilityGone(scrollview_HommeVoice)


        }
        btn_ChoixVoiceFemme3.setOnClickListener {

            setTextInfo(btn_ChoixVoiceFemme1.text.toString(),"$basePath/voiceFemale1.mp3")
            currentApiKey="rkuaKPXfXecaq04fVRwp"
            setViewVisibilityGone(scrollview_FemmeVoice)
            setViewVisibilityGone(scrollview_HommeVoice)


        }
        btn_ChoixVoiceFemme4.setOnClickListener {
            setTextInfo(btn_ChoixVoiceFemme2.text.toString(),"$basePath/voiceFemale2.mp3")
            currentApiKey="vPWvudfmcERCT22lGQ09"
            setViewVisibilityGone(scrollview_FemmeVoice)
            setViewVisibilityGone(scrollview_HommeVoice)

        }

        btn_ChoixVoiceHomme1.setOnClickListener {
            setTextInfo(btn_ChoixVoiceHomme1.text.toString(),"$basePath/voiceMale1.mp3")
            //currentApiKey="qUsB63LIpy65fSmt72zk"
            currentApiKey="mInzqBAaPI3P4wEbi9DD"
            setViewVisibilityGone(scrollview_HommeVoice)
            setViewVisibilityGone(scrollview_FemmeVoice)


        }
        btn_ChoixVoiceHomme2.setOnClickListener {

            setTextInfo(btn_ChoixVoiceHomme2.text.toString(),"$basePath/voiceMale2.mp3")
            currentApiKey="JoAXY9Z9JBJC8OP2xjtB"
            setViewVisibilityGone(scrollview_HommeVoice)
            setViewVisibilityGone(scrollview_FemmeVoice)

        }
        btn_ChoixVoiceHomme3.setOnClickListener {
            setTextInfo(btn_ChoixVoiceHomme1.text.toString(),"$basePath/voiceMale1.mp3")
            currentApiKey="DFNAdQgS6iGcQrnb7YDy"
            setViewVisibilityGone(scrollview_HommeVoice)
            setViewVisibilityGone(scrollview_FemmeVoice)


        }
        btn_ChoixVoiceHomme4.setOnClickListener {

            setTextInfo(btn_ChoixVoiceHomme2.text.toString(),"$basePath/voiceMale2.mp3")
            currentApiKey="N3JujXoDbbcHGwOGpdAp"
            setViewVisibilityGone(scrollview_HommeVoice)
            setViewVisibilityGone(scrollview_FemmeVoice)

        }

        btn_ok.setOnClickListener {
            if(curentVoice==null){
                Toast.makeText(this, "Selectionner une voix", Toast.LENGTH_SHORT).show()

            }else{

                val intent = Intent(this, Step2::class.java)
                intent.putExtra("curentVoice", curentVoice)
                Log.i("test12345678", "onCreate: "+curentVoice.toString())
                intent.putExtra("nom", nom)
                intent.putExtra("curentAPIKey", currentApiKey)

                startActivity(intent)


            }


        }

    }

    private fun setTextInfo(text : String,currentVoice : String){
        val textInfo = findViewById<TextView>(R.id.textViewStep2)
        curentVoice = currentVoice
        textInfo.setText(text)
    }

    private fun setViewVisibility(scrollview : ScrollView){

        if(scrollview.visibility== View.GONE){
            scrollview.visibility = View.VISIBLE
        }
        else {
            scrollview.visibility = View.GONE
        }
    }
    private fun setViewVisibilityGone(scrollview : ScrollView){

        if(scrollview.visibility== View.VISIBLE){
            scrollview.visibility = View.GONE
        }

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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}