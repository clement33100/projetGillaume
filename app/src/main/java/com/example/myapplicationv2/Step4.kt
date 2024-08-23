package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextClock
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Step4 : AppCompatActivity() {

    private lateinit var btn_valider: Button

    private lateinit var numberPickerMinutes: NumberPicker
    private lateinit var numberPickerSeconds: NumberPicker
    private lateinit var numberPickerHours: NumberPicker
    private var selectedDurationInSeconds: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_step4)

        numberPickerMinutes = findViewById(R.id.numberPickerMinutes)
        numberPickerSeconds = findViewById(R.id.numberPickerSeconds)
        numberPickerHours = findViewById(R.id.numberPickerHours)
        // Configurer les NumberPickers dans le code
        numberPickerMinutes.minValue = 0
        numberPickerMinutes.maxValue = 59

        numberPickerSeconds.minValue = 0
        numberPickerSeconds.maxValue = 59

        numberPickerHours.minValue=0
        numberPickerHours.maxValue=23



        val filePaths = intent.getStringExtra("filePaths")

        Log.i("test12345", "onCreate: "+filePaths.toString())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_valider=findViewById<Button>(R.id.btn_Valid)

        btn_valider.setOnClickListener {



            val hours = numberPickerHours.value
            val minutes = numberPickerMinutes.value
            val seconds = numberPickerSeconds.value
            selectedDurationInSeconds = hours * 3600 + minutes * 60 + seconds

            // Passer directement les données à Step5
            if (filePaths != null && selectedDurationInSeconds > 30  ) {
                // Passer les données à l'activité suivante
                    val intent = Intent(this, MeditationPlay::class.java).apply {
                    putExtra("filePaths", filePaths)

                    putExtra("selectedDuration", selectedDurationInSeconds)
                    Log.i("test12345", "onCreate: "+selectedDurationInSeconds.toString())
                }
                startActivity(intent)
            } else {
                // Afficher un message d'erreur ou gérer le cas où filePaths est null
                Toast.makeText(this, "Please select a valid time.", Toast.LENGTH_SHORT).show()            }
        }


    }
}