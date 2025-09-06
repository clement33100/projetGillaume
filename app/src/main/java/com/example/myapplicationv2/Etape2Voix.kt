package com.example.myapplicationv2

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.util.Locale

class Etape2Voix : Base() {  // Hérite de Base
    private lateinit var btn_VoiceFemme: Button
    private lateinit var btn_VoiceHomme: Button
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentTempM4a: File? = null

    private fun audioDir(): File {
        val dir = File(filesDir, "audio_rec")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun tempM4a(tag: String) = File(audioDir(), "rec_${tag}.m4a")
    private fun outMp3(tag: String) = File(audioDir(), "rec_${tag}.mp3")
    private enum class Gender { FEMALE, MALE }
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



        // Icônes claires sur barre d’état sombre
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // Initialisation des vues
        btn_VoiceFemme = findViewById(R.id.btn_voix_femme)
        btn_VoiceHomme = findViewById(R.id.btn_voix_homme)
        //scrollview_FemmeVoice = findViewById(R.id.scrollViewFemmeVoice)
        //scrollview_HommeVoice = findViewById(R.id.scrollViewHommeVoice)

        //btn_ChoixVoiceFemme1 = findViewById(R.id.voixFemme1)
        //btn_ChoixVoiceFemme2 = findViewById(R.id.voixFemme2)

        //btn_ChoixVoiceHomme1 = findViewById(R.id.voixHomme1)
        //btn_ChoixVoiceHomme2 = findViewById(R.id.voixHomme2)

        btn_ok = findViewById(R.id.btn_ok_step2)

        val nom = intent.getStringExtra("nom")
        Log.i("TAG", "onCreate: " + nom.toString())

        /*btn_VoiceFemme.setOnClickListener {
            setViewVisibility(scrollview_FemmeVoice)
        }

        btn_VoiceHomme.setOnClickListener {
            setViewVisibility(scrollview_HommeVoice)
        }*/

        val basePath = filesDir.absolutePath + "/audio/"
        Log.i("TEST123", "path : " + basePath)

        clearAudioDir()

        btn_VoiceFemme.setOnClickListener {
            setTextInfo(btn_VoiceFemme.text.toString(), "$basePath/voiceFemale.mp3")
            currentApiKey = pickApiKeyByLang(Gender.FEMALE)


            playAudioFromRaw(R.raw.voixfemme)   // choisi fr/en/es selon la locale

        }
        /*btn_ChoixVoiceFemme2.setOnClickListener {
            setTextInfo(btn_ChoixVoiceFemme2.text.toString(), "$basePath/voiceFemale2.mp3")
            currentApiKey = "huLASkSLzY35zexSYP1g "
            setViewVisibilityGone(scrollview_FemmeVoice)
            setViewVisibilityGone(scrollview_HommeVoice)
            playAudioFromRaw(R.raw.voixfemme2)
        }*/

        btn_VoiceHomme.setOnClickListener {
            setTextInfo(btn_VoiceHomme.text.toString(), "$basePath/voiceMale.mp3")
            currentApiKey = pickApiKeyByLang(Gender.MALE)


            playAudioFromRaw(R.raw.voixhomme)   // choisi fr/en/es selon la locale
        }

        /*btn_ChoixVoiceHomme2.setOnClickListener {
            setTextInfo(btn_ChoixVoiceHomme2.text.toString(), "$basePath/voiceMale2.mp3")
            currentApiKey = "JoAXY9Z9JBJC8OP2xjtB"
            setViewVisibilityGone(scrollview_HommeVoice)
            setViewVisibilityGone(scrollview_FemmeVoice)
            playAudioFromRaw(R.raw.voixhomme2)
        }*/
        val btnTavoix = findViewById<Button>(R.id.btn_tavoix) // remplace par l'ID exact de ton bouton
        btnTavoix.setOnClickListener {

            val intent = Intent(this,Tavoix::class.java)
            startActivity(intent)
        }

        val intention = intent.getBooleanExtra("intention", false)

        btn_ok.setOnClickListener {

            Log.d("tatata", "onCreate: "+currentApiKey.toString())

            if (!isOnline()) {
                Toast.makeText(this@Etape2Voix, getString(R.string.toastDisconected)
                    , Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener          // ← on ne lance pas l'activité
            }

            if (curentVoice == null) {
                Toast.makeText(this, getString(R.string.toastSelectVoice)
                    , Toast.LENGTH_SHORT).show()
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


    private fun clearAudioDir() {
        val audioDir = File(filesDir, "audio")
        if (audioDir.exists() && audioDir.isDirectory) {
            audioDir.listFiles()?.forEach { file ->
                try {
                    file.delete()
                    Log.d("AppInit", "Deleted: ${file.absolutePath}")
                } catch (e: Exception) {
                    Log.e("AppInit", "Failed to delete: ${file.absolutePath} -> ${e.message}")
                }
            }
        }
    }


    private fun pickApiKeyByLang(gender: Gender): String {
        // Option 1 : langue passée par Intent ("fr", "en", "es"), sinon langue du device
        val lang = intent.getStringExtra("lang") ?: Locale.getDefault().language.lowercase()

        return when (gender) {
            Gender.FEMALE -> when (lang) {
                "en" -> BuildConfig.ELEVENLABS_API_KEY_FEMME_EN
                "es" -> BuildConfig.ELEVENLABS_API_KEY_FEMME_ES
                else -> BuildConfig.ELEVENLABS_API_KEY_FEMME // défaut FR
            }
            Gender.MALE -> when (lang) {
                "en" -> BuildConfig.ELEVENLABS_API_KEY_HOMME_EN
                "es" -> BuildConfig.ELEVENLABS_API_KEY_HOMME_ES
                else -> BuildConfig.ELEVENLABS_API_KEY_HOMME // défaut FR
            }
        }
    }


    private fun ensureRecordPermission(): Boolean {
        val perm = android.Manifest.permission.RECORD_AUDIO
        return if (checkSelfPermission(perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestPermissions(arrayOf(perm), 1234)
            false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1234 && (grantResults.firstOrNull() ?: -1) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Micro refusé : impossible d’enregistrer.", Toast.LENGTH_LONG).show()
        }
    }
    private fun startRecording(tag: String) {
        if (isRecording) return
        if (!ensureRecordPermission()) return

        try {
            currentTempM4a = tempM4a(tag).apply { if (exists()) delete() }
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)   // 128 kbps
                setAudioSamplingRate(44100)
                setOutputFile(currentTempM4a!!.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            Toast.makeText(this, "Enregistrement… (maintiens appuyé)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur démarrage enregistrement: ${e.message}", Toast.LENGTH_LONG).show()
            stopRecordingInternal()
        }
    }

    private fun stopRecordingAndConvert(tag: String) {
        if (!isRecording) return
        stopRecordingInternal()

        val inFile = currentTempM4a ?: return
        val outFile = outMp3(tag).apply { if (exists()) delete() }

        // Conversion via FFmpegKit (asynchrone)
        val cmd = "-y -i ${inFile.absolutePath} -codec:a libmp3lame -q:a 2 ${outFile.absolutePath}"
        com.arthenica.ffmpegkit.FFmpegKit.executeAsync(cmd) { session ->
            val state = session.state
            val returnCode = session.returnCode
            runOnUiThread {
                if (com.arthenica.ffmpegkit.ReturnCode.isSuccess(returnCode)) {
                    Toast.makeText(this, "Sauvegardé : ${outFile.name}", Toast.LENGTH_LONG).show()
                    Log.d("REC", "MP3: ${outFile.absolutePath}")
                } else {
                    Toast.makeText(this, "Conversion MP3 échouée (${state} / ${returnCode}).", Toast.LENGTH_LONG).show()
                }
            }
            // Option: supprimer le .m4a temporaire
            inFile.delete()
        }
    }

    private fun stopRecordingInternal() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) { /* ignore */ }
        mediaRecorder = null
        isRecording = false
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
            //Toast.makeText(this, "Lecture audio", Toast.LENGTH_SHORT).show()
        } else {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer = null
                //Toast.makeText(this, "Arrêt audio", Toast.LENGTH_SHORT).show()
                mediaPlayer = MediaPlayer.create(this, audioResId)
                mediaPlayer?.start()
            } else {
                mediaPlayer = MediaPlayer.create(this, audioResId)
                mediaPlayer?.start()
                //Toast.makeText(this, "Lecture audio", Toast.LENGTH_SHORT).show()
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

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Android M (23) et + : NetworkCapabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
        // Avant M
        @Suppress("DEPRECATION")
        val info = cm.activeNetworkInfo
        @Suppress("DEPRECATION")
        return info != null && info.isConnected
    }

}