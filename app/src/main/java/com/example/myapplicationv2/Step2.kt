package com.example.myapplicationv2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Step2 : AppCompatActivity() {
    private lateinit var container: LinearLayout
    private var textViewCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_step2)

        container = findViewById(R.id.container)
        val addButton = findViewById<Button>(R.id.addButton)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addButton.setOnClickListener {
            Log.d("Stape2", "Button clicked")
            showAddTextDialog()
        }
    }

    private fun showAddTextDialog() {
        Log.d("Stape2", "Showing dialog")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ajouter un texte")

        // Set up the input
        val input = EditText(this)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Valider") { dialog, which ->
            val text = input.text.toString()
            addTextView(text)
        }
        builder.setNegativeButton("Annuler") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun addTextView(text: String) {
        textViewCount++
        val numberedText = "Affirmation $textViewCount : Tu es $text"
        Log.d("Stape2", "Adding TextView with text: $numberedText")
        val textView = TextView(this)
        textView.text = numberedText
        textView.textSize = 18f
        textView.setPadding(8, 8, 8, 8)
        container.addView(textView)
    }
}
