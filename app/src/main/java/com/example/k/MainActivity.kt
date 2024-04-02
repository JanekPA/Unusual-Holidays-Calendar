package com.example.k

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.example.k.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var logoutButton: Button
    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.Logout_button)
        logoutButton.setOnClickListener { logout() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        binding.SideMenuButton?.setOnClickListener{
            val intent = Intent(this,SideMenu::class.java)
            startActivity(intent)
        }
    }


    private fun logout() {
        firebaseAuth.signOut()
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        // Show a toast message indicating successful logout
        Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
    }
}