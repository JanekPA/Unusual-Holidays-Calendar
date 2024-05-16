package com.example.k

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityEditNotesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditNotes : AppCompatActivity() {

    private lateinit var binding: ActivityEditNotesBinding
    private lateinit var notesEditText: EditText
    private var dateKey: String? = null
    private var text: String? = null
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notesEditText = findViewById(R.id.notesEditText)

        dateKey = intent.getStringExtra("dateKey")
        text = intent.getStringExtra("text")
        notesEditText.setText(text)

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveNotes()
        }
    }


    private fun saveNotes() {

        val newText = notesEditText.text.toString()

        if (newText.length > 200) {
            Toast.makeText(this, "Note cannot exceed 200 characters!", Toast.LENGTH_LONG).show()
        } else {
            val firebaseAuth = FirebaseAuth.getInstance()
            val firebaseUser = firebaseAuth.currentUser

            firebaseUser?.let { user ->
                val uid = user.uid

                val firebaseRef = FirebaseDatabase.getInstance().getReference("Notes")

                    firebaseRef.child(uid).child(dateKey!!).child("text").removeValue().addOnCompleteListener {
                        firebaseRef.child(uid).child(dateKey!!).child("text").setValue(newText)
                            .addOnCompleteListener {
                                Toast.makeText(this, "Data add successfully!", Toast.LENGTH_SHORT)
                                    .show()
                                finish()
                                startActivity(
                                    Intent(
                                        this@EditNotes,
                                        MainActivity::class.java
                                    )
                                )
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    this,
                                    "Error: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                }
            }
        }
    }
}
