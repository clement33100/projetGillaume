package com.example.myapplicationv2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Etape2Voix : Base() {  // Hérite de Base
    private lateinit var btn_VoiceFemme: Button
    private lateinit var btn_VoiceHomme: Button

    private lateinit var btn_ChoixVoiceHomme1: Button
    private lateinit var btn_ChoixVoiceHomme2: Button

    private lateinit var btn_ChoixVoiceFemme1: Button
    private lateinit var btn_ChoixVoiceFemme2: Button

    private lateinit var btn_ok: Button
    private var currentFilePath: String? = null

    private lateinit var scrollview_FemmeVoice: View
    private lateinit var scrollview_HommeVoice: View
    private var mediaPlayer: MediaPlayer? = null

    private var curentVoice: String? = null
    private var currentApiKey: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_etape2_voix  // Utilisez le layout adapté
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialisation des vues
        btn_VoiceFemme = findViewById(R.id.btn_voix_femme)
        btn_VoiceHomme = findViewById(R.id.btn_voix_homme)
        scrollview_FemmeVoice = findViewById(R.id.scrollViewFemmeVoice)
        scrollview_HommeVoice = findViewById(R.id.scrollViewHommeVoice)

        btn_ChoixVoiceFemme1 = findViewById(R.id.voixFemme1)
        btn_ChoixVoiceFemme2 = findViewById(R.id.voixFemme2)

        btn_ChoixVoiceHomme1 = findViewById(R.id.voixHomme1)
        btn_ChoixVoiceHomme2 = findViewById(R.id.voixHomme2)

        btn_ok = findViewById(R.id.btn_ok_step2)

        val nom = intent.getStringExtra("nom")
        Log.i("TAG", "onCreate: " + nom.toString())

        btn_VoiceFemme.setOnClickListener {
            setViewVisibility(scrollview_FemmeVoice)
        }

        btn_VoiceHomme.setOnClickListener {
            setViewVisibility(scrollview_HommeVoice)
        }

        val basePath = filesDir.absolutePath + "/audio/"
        Log.i("TEST123", "path : " + basePath)

        btn_ChoixVoiceFemme1.setOnClickListener {
            setTextInfo(btn_ChoixVoiceFemme1.text.toString(), "$basePath/voiceFemale1.mp3")
            currentApiKey = "hGwGBgvO4Leffvcjs2vm"
            setViewVisibilityGone(scrollview_FemmeVoice)
            setViewVisibilityGone(scrollview_HommeVoice)
            playAudioFromRaw(R.raw.voixfemme1)
        }

        btn_ChoixVoiceFemme2.setOnClickListener {
            setTextInfo(btn_ChoixVoiceFemme2.text.toString(), "$basePath/voiceFemale2.mp3")
            currentApiKey = "huLASkSLzY35zexSYP1g "
            setViewVisibilityGone(scrollview_FemmeVoice)
            setViewVisibilityGone(scrollview_HommeVoice)
            playAudioFromRaw(R.raw.voixfemme2)
        }

        btn_ChoixVoiceHomme1.setOnClickListener {
            setTextInfo(btn_ChoixVoiceHomme1.text.toString(), "$basePath/voiceMale1.mp3")
            currentApiKey = "D2iqIAl6i5hrD9q2faCp"
            setViewVisibilityGone(scrollview_HommeVoice)
            setViewVisibilityGone(scrollview_FemmeVoice)
            playAudioFromRaw(R.raw.voixhomme1)
        }

        btn_ChoixVoiceHomme2.setOnClickListener {
            setTextInfo(btn_ChoixVoiceHomme2.text.toString(), "$basePath/voiceMale2.mp3")
            currentApiKey = "JoAXY9Z9JBJC8OP2xjtB"
            setViewVisibilityGone(scrollview_HommeVoice)
            setViewVisibilityGone(scrollview_FemmeVoice)
            playAudioFromRaw(R.raw.voixhomme2)
        }

        val intention = intent.getBooleanExtra("intention", false)

        btn_ok.setOnClickListener {
            if (curentVoice == null) {
                Toast.makeText(this, "Sélectionner une voix", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, Step2::class.java)
                intent.putExtra("curentVoice", curentVoice)
                Log.i("test12345678", "onCreate: " + curentVoice.toString())
                intent.putExtra("nom", nom)
                intent.putExtra("curentAPIKey", currentApiKey)
                intent.putExtra("intention", intention)
                startActivity(intent)
            }
        }

        // Gérer les insets pour le layout (si nécessaire)
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
    }

    private fun setTextInfo(text: String, currentVoice: String) {
        val textInfo = findViewById<TextView>(R.id.textViewStep2)
        curentVoice = currentVoice
        textInfo.text = text
    }

    private fun setViewVisibility(scrollview: View) {
        scrollview.visibility = if (scrollview.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun setViewVisibilityGone(scrollview: View) {
        if (scrollview.visibility == View.VISIBLE) {
            scrollview.visibility = View.GONE
        }
    }

    private fun playAudioFromRaw(audioResId: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, audioResId)
            mediaPlayer?.start()
            Toast.makeText(this, "Lecture audio", Toast.LENGTH_SHORT).show()
        } else {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer = null
                Toast.makeText(this, "Arrêt audio", Toast.LENGTH_SHORT).show()
                mediaPlayer = MediaPlayer.create(this, audioResId)
                mediaPlayer?.start()
            } else {
                mediaPlayer = MediaPlayer.create(this, audioResId)
                mediaPlayer?.start()
                Toast.makeText(this, "Lecture audio", Toast.LENGTH_SHORT).show()
            }
        }
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
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
