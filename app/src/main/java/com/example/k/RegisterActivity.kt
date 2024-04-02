package com.example.k

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityRegisterNewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.DatabaseReference
import com.example.k.models.PersonalizationData
import com.google.firebase.database.FirebaseDatabase


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterNewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.redirectToLog.setOnClickListener {
            val intent2 = Intent(this, SignInActivity::class.java)
            startActivity(intent2)
        }
        binding.registerclick.setOnClickListener {
            val name = binding.textLoginName.text.toString()
            val email = binding.textEmail.text.toString()
            val pass = binding.textRegisterpassword.text.toString()
            val user = binding.textUsername.text.toString()

            // Validate email format
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            firebaseRef = FirebaseDatabase.getInstance().getReference("UsersPersonalization")
            if (email.isNotEmpty() && pass.isNotEmpty() && user.isNotEmpty() && name.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val sharedPreferences = getSharedPreferences("RegData",Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("nickname", user)
                        editor.apply()
                        val datas = PersonalizationData(
                        user
                        )

                        firebaseRef.child(user).setValue(datas)
                        val intent = Intent(this, PersonalizationActivity::class.java)
                        startActivity(intent)
                    } else {
                        val exception = task.exception
                        if (exception is FirebaseAuthInvalidCredentialsException) {
                            // Password doesn't meet the Firebase standards
                            Toast.makeText(this, "Password is too weak", Toast.LENGTH_SHORT).show()
                        } else {
                            // Other errors, such as network issues, server error, etc.
                            Toast.makeText(this, exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return email.matches(emailRegex)
    }
}