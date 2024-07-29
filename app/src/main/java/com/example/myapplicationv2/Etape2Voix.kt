package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
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

class Etape2Voix : AppCompatActivity() {
    private lateinit var btn_VoiceFemme: Button
    private lateinit var btn_VoiceHomme: Button

    private lateinit var btn_ChoixVoiceHomme1: Button
    private lateinit var btn_ChoixVoiceHomme2: Button

    private lateinit var btn_ChoixVoiceFemme1: Button
    private lateinit var btn_ChoixVoiceFemme2: Button

    private lateinit var btn_ok: Button


    private lateinit var scrollview_FemmeVoice: ScrollView
    private lateinit var scrollview_HommeVoice: ScrollView
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var voicefemme1: ImageButton
    private lateinit var voicefemme2: ImageButton

    private lateinit var voicehomme1: ImageButton
    private lateinit var voicehomme2: ImageButton

    private var curentVoice: Int?=null



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
        voicehomme1 = findViewById<ImageButton>(R.id.listenHomme1)
        voicehomme2 = findViewById<ImageButton>(R.id.listenHomme2)

        btn_ChoixVoiceFemme1 = findViewById<Button>(R.id.voixFemme1)
        btn_ChoixVoiceFemme2 = findViewById<Button>(R.id.voixFemme2)

        btn_ChoixVoiceHomme1 = findViewById<Button>(R.id.voixHomme1)
        btn_ChoixVoiceHomme2 = findViewById<Button>(R.id.voixHomme2)

        btn_ok = findViewById<Button>(R.id.btn_ok_step2)


        btn_VoiceFemme.setOnClickListener {
           setViewVisibility(scrollview_FemmeVoice)
        }

        btn_VoiceHomme.setOnClickListener {
            setViewVisibility(scrollview_HommeVoice)
        }

        voicefemme1.setOnClickListener{
            playAudioFromRaw(R.raw.voixfemme1)
        }
        voicefemme2.setOnClickListener{
            playAudioFromRaw(R.raw.voixfemme2)
        }
        voicehomme1.setOnClickListener{
            playAudioFromRaw(R.raw.voixhomme1)
        }
        voicehomme2.setOnClickListener{
            playAudioFromRaw(R.raw.voixhomme2)
        }

        btn_ChoixVoiceFemme1.setOnClickListener {

            setTextInfo(btn_ChoixVoiceFemme1.text.toString(),R.raw.voixfemme1)

        }
        btn_ChoixVoiceFemme2.setOnClickListener {
            setTextInfo(btn_ChoixVoiceFemme2.text.toString(),R.raw.voixfemme2)


        }
        btn_ChoixVoiceHomme1.setOnClickListener {
            setTextInfo(btn_ChoixVoiceHomme1.text.toString(),R.raw.voixhomme1)


        }
        btn_ChoixVoiceHomme2.setOnClickListener {

            setTextInfo(btn_ChoixVoiceHomme2.text.toString(),R.raw.voixhomme2)

        }

        btn_ok.setOnClickListener {
            if(curentVoice==null){
                Toast.makeText(this, "Selectionner une voix", Toast.LENGTH_SHORT).show()

            }else{

                val intent = Intent(this, Step2::class.java)
                intent.putExtra("curentVoice", curentVoice)

                startActivity(intent)


            }


        }

    }

    private fun setTextInfo(text : String,audioResId: Int){
        val textInfo = findViewById<TextView>(R.id.textViewStep2)
        curentVoice = audioResId
        textInfo.setText("Ton choix : "+text)
    }

    private fun setViewVisibility(scrollview : ScrollView){

        if(scrollview.visibility== View.GONE){
            scrollview.visibility = View.VISIBLE
        }
        else {
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}