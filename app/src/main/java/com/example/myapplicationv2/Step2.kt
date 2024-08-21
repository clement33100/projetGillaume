package com.example.myapplicationv2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Step2 : Base() {  // Hérite de Base au lieu de AppCompatActivity
    private lateinit var container: LinearLayout
    private var textViewCount = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_step2  // Retourne le layout spécifique à cette activité
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configuration du bouton de navigation dans Base, ne plus toucher à btn_burger ici

        container = findViewById(R.id.container)
        val addButton = findViewById<Button>(R.id.addButton)

        // Gestion des insets pour le layout
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addButton.setOnClickListener {
            Log.d("Step2", "Button clicked")
            showAddTextDialog()
        }
    }

    private fun showAddTextDialog() {
        Log.d("Step2", "Showing dialog")
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
        val numberedText = "Affirmation $textViewCount : $text"
        Log.d("Step2", "Adding TextView with text: $numberedText")

        // Create a new horizontal LinearLayout
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.HORIZONTAL

        // Create the TextView
        val textView = TextView(this)
        textView.text = numberedText
        textView.textSize = 18f
        textView.setPadding(8, 30, 8, 0)

        // Create the delete ImageButton
        val deleteButton = ImageButton(this)
        deleteButton.setImageResource(R.drawable.imgbtn_delete)
        deleteButton.setBackgroundColor(0x00000000)  // Make background transparent
        deleteButton.setOnClickListener {
            container.removeView(linearLayout)
        }

        // Add TextView and ImageButton to the horizontal LinearLayout
        linearLayout.addView(textView)
        linearLayout.addView(deleteButton)

        // Add the horizontal LinearLayout to the container
        container.addView(linearLayout)
    }
}
