package com.example.k

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddHolidayActivity : AppCompatActivity() {

    private lateinit var dateEditText: EditText
    private lateinit var holidayNameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_holiday)

        dateEditText = findViewById(R.id.dateEditText)
        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val date = dateEditText.text.toString()
            val holidayName = holidayNameEditText.text.toString()
            if (date.isNotEmpty() && holidayName.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance()
                val holidaysRef = database.getReference("HolidayNames")
                holidaysRef.child(date).setValue(holidayName).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Holiday saved!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Write error!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}