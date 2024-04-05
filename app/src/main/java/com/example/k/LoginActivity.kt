package com.example.k

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityLoginNewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginNewBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.redirectToReg.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.loginclick.setOnClickListener {
            val email = binding.textLogin.text.toString()
            val pass = binding.textLoginpassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        when (val exception = task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                // Invalid email format
                                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed", Toast.LENGTH_SHORT).show()
            }
        }

        // Add click listener for "devlog" button
        binding.devlog.setOnClickListener {
            val devEmail = "email@gmail.com"
            val devPassword = "Password"

            firebaseAuth.signInWithEmailAndPassword(devEmail, devPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "DevLogin", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Handle login failure
                    Toast.makeText(this, "Failed to log in with developer credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
