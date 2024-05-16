package com.example.k

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityAddNotesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddNotes : AppCompatActivity() {

    private lateinit var binding: ActivityAddNotesBinding
    private lateinit var calendarView: CalendarView
    private lateinit var NotesEditText: EditText
    private lateinit var selectedDate: String
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendarView = findViewById(R.id.calendarV)
        NotesEditText = findViewById(R.id.notesEditText)

        selectedDate = ""

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "${dayOfMonth.toString().padStart(2, '0')}-${
                (month + 1).toString().padStart(2, '0')
            }"
        }

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveNotes()
        }
    }


    private fun saveNotes() {
        val text = NotesEditText.text.toString()

        if (selectedDate.isEmpty())
        {
            Toast.makeText(this, "Please select a date!", Toast.LENGTH_LONG).show()
        }
        else if (text.length > 200)
        {
            Toast.makeText(this, "Note cannot exceed 200 characters!", Toast.LENGTH_LONG).show()
        }
        else
        {
            val firebaseAuth = FirebaseAuth.getInstance()
            val firebaseUser = firebaseAuth.currentUser

            firebaseUser?.let { user ->
                val uid = user.uid
                val firebaseRef = FirebaseDatabase.getInstance().getReference("Notes").child(uid).child(selectedDate)

                firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                            Toast.makeText(this@AddNotes, "A note for this date already exists!", Toast.LENGTH_LONG).show()
                        }
                        else
                        {
                            firebaseRef.child("text").setValue(text)
                                .addOnCompleteListener {
                                    Toast.makeText(this@AddNotes, "Data added successfully!", Toast.LENGTH_SHORT).show()
                                    finish()
                                    startActivity(Intent(this@AddNotes, MainActivity::class.java))
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this@AddNotes, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@AddNotes, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}