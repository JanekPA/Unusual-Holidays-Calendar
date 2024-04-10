package com.example.k

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.k.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        setupViews()


        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_preferences -> {

                    val intent = Intent(this, PrefChanging::class.java)
                    startActivity(intent)
                }
                R.id.nav_profile -> {

                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
                R.id.nav_settings -> {

                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                }
                R.id.Logout_button -> logout()

            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun setupViews() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.AccountButton?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.CalendarV?.setOnClickListener {
            val intent = Intent(this, CalendarView::class.java)
            startActivity(intent)
        }

        drawerLayout = binding.myDrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun logout() {
        firebaseAuth.signOut()
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        val preferences = getSharedPreferences("checkbox",MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("remember","false")
        editor.apply()
        finish()
        Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
    }
}
