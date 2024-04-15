package com.example.k

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class EditHolidayActivity : AppCompatActivity() {

    private lateinit var dateEditText: EditText
    private lateinit var holidayNameEditText: EditText
    private lateinit var updateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_holiday)

        dateEditText = findViewById(R.id.editDateEditText)
        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        updateButton = findViewById(R.id.updateButton)

        val originalDate = intent.getStringExtra("date")
        val holidayName = intent.getStringExtra("holidayName")
        dateEditText.setText(originalDate)
        holidayNameEditText.setText(holidayName)

        updateButton.setOnClickListener {
            val newDate = dateEditText.text.toString().trim()
            val newHolidayName = holidayNameEditText.text.toString().trim()

            if (!newDate.matches(Regex("\\b(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[012])\\b"))) {
                Toast.makeText(this, "Date must be in DD-MM format!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (newHolidayName.isEmpty()) {
                Toast.makeText(this, "Holiday name cannot be empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val holidaysRef = database.getReference("HolidayNames")

            holidaysRef.child(originalDate!!).removeValue().addOnSuccessListener {
                holidaysRef.child(newDate).setValue(newHolidayName).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Holiday updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}