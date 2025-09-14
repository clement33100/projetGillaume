package com.example.myapplicationv2

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class mesAffirmations : AppCompatActivity() {

    private lateinit var btnNew: Button
    private lateinit var btnMyaffirmations: Button
    private lateinit var btnHowItWorks: Button
    private lateinit var btnAdvices: Button
    private lateinit var btnAffirmationsDetails: Button
    private lateinit var textView: TextView

    fun getLayoutId(): Int = R.layout.activity_mesaffirmations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        // 1) Edge-to-edge et status bar
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView)
            ?.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)

        // 2) Init des vues
        btnNew = findViewById(R.id.btn_new)
        btnHowItWorks = findViewById(R.id.btn_howitworks)
        btnAdvices = findViewById(R.id.btn_advicesaffirmations)
        btnAffirmationsDetails = findViewById(R.id.btn_affirmation)
        textView = findViewById(R.id.textView9)

        // 3) Texte dynamique selon la langue + soulignement
        textView.text = getString(R.string.lien_texte)
        applyUnderlineStyle(textView)

        // 4) Clic sur tout le textView
        textView.setOnClickListener {
            val url = getString(R.string.linkform)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // 5) Listeners des boutons
        btnNew.setOnClickListener { startActivity(Intent(this, Etape2Voix::class.java)) }
        btnHowItWorks.setOnClickListener { startActivity(Intent(this, hdiw1::class.java)) }
        btnAdvices.setOnClickListener { startActivity(Intent(this, Advices::class.java)) }
        btnAffirmationsDetails.setOnClickListener {
            startActivity(Intent(this, mesAffirmationsDetails::class.java))
        }

        // 6) Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // 7) Changement de langue (live update, pas de recréation)
        val tvFrench   = findViewById<TextView>(R.id.textView2)
        val tvEnglish  = findViewById<TextView>(R.id.textView5)
        val tvSpanish  = findViewById<TextView>(R.id.textView10)
        tvFrench.setOnClickListener  { switchLanguage("fr") }
        tvEnglish.setOnClickListener { switchLanguage("en") }
        tvSpanish.setOnClickListener { switchLanguage("es") }

        // 8) Met à jour tous les textes au démarrage
        updateTexts()
    }

    // Empêche la recréation auto de l'activité et rafraîchit seulement les textes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateTexts()
    }

    private fun switchLanguage(tag: String) {
        val newLocales = LocaleListCompat.forLanguageTags(tag)
        if (AppCompatDelegate.getApplicationLocales() == newLocales) return

        // 1) Change la locale (persistant pour l’appli)
        AppCompatDelegate.setApplicationLocales(newLocales)

        // 2) Recharge les textes visibles (sans recréation)
        updateTexts()
    }

    private fun updateTexts() {
        // Boutons
        btnNew.text = getString(R.string.btn_create_new)
        btnHowItWorks.text = getString(R.string.btn_how_it_works)
        btnAdvices.text = getString(R.string.btn_practical_advices)
        btnAffirmationsDetails.text = getString(R.string.btn_my_affirmations)

        // Texte avec lien + soulignement selon la langue
        textView.text = getString(R.string.lien_texte)
        applyUnderlineStyle(textView)
    }

    // --- Utilitaires soulignement ---

    private fun applyUnderlineStyle(tv: TextView) {
        val fullText = tv.text.toString()
        val (start, end) = computeUnderlineRange(fullText)
        if (start >= 0 && end > start) {
            val ss = SpannableString(fullText)
            ss.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ss.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.green)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = ss
        }
    }

    private fun computeUnderlineRange(fullText: String): Pair<Int, Int> = when {
        fullText.contains("en cliquant ici", ignoreCase = true) -> {
            val s = fullText.indexOf("en cliquant ici", ignoreCase = true)
            s to s + "en cliquant ici".length
        }
        fullText.contains("by clicking here", ignoreCase = true) -> {
            val s = fullText.indexOf("by clicking here", ignoreCase = true)
            s to s + "by clicking here".length
        }
        fullText.contains("haciendo clic aquí", ignoreCase = true) -> {
            val s = fullText.indexOf("haciendo clic aquí", ignoreCase = true)
            s to s + "haciendo clic aquí".length
        }
        else -> -1 to -1
    }
}
