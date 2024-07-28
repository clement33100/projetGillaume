package com.example.myapplicationv2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainPage : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var audioAdapter: AudioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_page)

        recyclerView = findViewById(R.id.recyclerView)

        // Simuler une liste de fichiers audio .mp4
        val audioFiles = listOf(
            "audio1.mp4",
            "audio2.mp4",
            "audio3.mp4",
            "audio4.mp4"
        )
        audioAdapter = AudioAdapter(this, audioFiles)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = audioAdapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}