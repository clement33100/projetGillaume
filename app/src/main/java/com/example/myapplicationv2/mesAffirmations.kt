package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class mesAffirmations : Base() {  // Hérite de Base au lieu de AppCompatActivity

    override fun getLayoutId(): Int {
        return R.layout.activity_mesaffirmations // Retourne le layout spécifique à cette activité
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupérer les vues
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = findViewById<TextView>(R.id.empty_view)

        // Supposons que vous ayez une liste de données à afficher
        val affirmations = listOf<String>()  // Remplacez par votre liste réelle

        if (affirmations.isEmpty()) {
            // Si la liste est vide, afficher le TextView et cacher le RecyclerView
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            // Si la liste n'est pas vide, afficher le RecyclerView et cacher le TextView
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE

            // Configurer le RecyclerView avec les données
            //val adapter = ButtonAdapter(affirmations) { label ->
                // Gérer le clic sur un bouton
                // Par exemple : Toast.makeText(this, "$label cliqué", Toast.LENGTH_SHORT).show()
            //}
            recyclerView.layoutManager = LinearLayoutManager(this)
            //recyclerView.adapter = adapter
        }
    }
}
