package com.example.myapplicationv2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class mesAffirmations : AppCompatActivity() {  // Hérite de Base au lieu de AppCompatActivity

    private lateinit var btnNew: Button
    private lateinit var btnMyaffirmations: Button
    private lateinit var btnHowItWorks: Button
    private lateinit var btnAdvices: Button
    private lateinit var btnAffirmationsDetails: Button

    fun getLayoutId(): Int {
        return R.layout.activity_mesaffirmations  // Utilisez le layout adapté
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1) Posez le layout
        setContentView(getLayoutId())

        // 2) Edge-to-edge et status bar
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView)
            ?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // 3) Init des vues
        btnNew = findViewById(R.id.btn_new)
        btnHowItWorks = findViewById(R.id.btn_howitworks)
        btnAdvices = findViewById(R.id.btn_advicesaffirmations)
        btnAffirmationsDetails = findViewById(R.id.btn_affirmation) // si c'est le bon ID
        val textView = findViewById<TextView>(R.id.textView9)

        // 4) Texte + span « Clique ici »
        textView.text = getString(R.string.lien_texte)
        val start = textView.text.indexOf("en cliquant")
        val end   = start + "en cliquant ici".length
        val ss = SpannableString(textView.text)
        ss.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.green)
            ), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = ss

        // 5) Clic sur tout le textView
        textView.setOnClickListener {
            startActivity(Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://forms.gle/RtJFDrkqoJrxEhzh6")
            ))
        }

        // 6) Listeners des boutons
        btnNew.setOnClickListener { startActivity(Intent(this, Etape2Voix::class.java)) }
        btnHowItWorks.setOnClickListener { startActivity(Intent(this, hdiw1::class.java)) }
        btnAdvices.setOnClickListener { startActivity(Intent(this, Advices::class.java)) }
        btnAffirmationsDetails.setOnClickListener {
            startActivity(Intent(this, mesAffirmationsDetails::class.java))
        }

        // 7) Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }
    }
}
