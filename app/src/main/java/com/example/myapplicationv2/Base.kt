package com.example.myapplicationv2

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.activity.enableEdgeToEdge


abstract class Base : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var btnBurger: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge Android 15 compatible
        enableEdgeToEdge()

        setContentView(getLayoutId())

        // Drawer + nav
        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleMenuItemClick(menuItem)
            drawerLayout.closeDrawers()
            true
        }

        // Bouton burger
        btnBurger = findViewById(R.id.btn_burger)
        btnBurger.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Appliquer les WindowInsets à la vue principale
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        // Pas de listener global ici → évite le double padding
    }

    abstract fun getLayoutId(): Int

    private fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.btn_affirm -> startActivity(Intent(this, mesAffirmations::class.java))
            R.id.btn_new -> startActivity(Intent(this, Etape2Voix::class.java))
            R.id.btn_myAffirm -> startActivity(Intent(this, mesAffirmationsDetails::class.java))
            R.id.btn_advices -> startActivity(Intent(this, Advices::class.java))
            R.id.btn_hdiw -> startActivity(Intent(this, hdiw1::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
