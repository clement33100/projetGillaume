package com.example.myapplicationv2

import android.os.Bundle

class ChoseBelieve : Base() {

    override fun getLayoutId(): Int {
        return R.layout.activity_chose_believe  // Retourne le layout spécifique à cette activité
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Le reste de la logique d'initialisation spécifique à ChoseBelieve
    }
}
