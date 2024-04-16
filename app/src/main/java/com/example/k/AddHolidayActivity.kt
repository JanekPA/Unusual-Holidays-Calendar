package com.example.k

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddHolidayActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var holidayNameEditText: EditText
    private fun Int.pad(digits: Int) = toString().padStart(digits, '0')

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_holiday)

        calendarView = findViewById(R.id.calendarV)
        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        var selectedDate = ""

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val formattedDate = "${dayOfMonth.pad(2)}-${(month + 1).pad(2)}"
            selectedDate = formattedDate
        }

        saveButton.setOnClickListener {
            val holidayName = holidayNameEditText.text.toString().trim()

            if (holidayName.isEmpty()) {
                Toast.makeText(this, "Holiday name cannot be empty!", Toast.LENGTH_LONG).show()
            } else if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date!", Toast.LENGTH_LONG).show()
            } else {
                val database = FirebaseDatabase.getInstance()
                val holidaysRef = database.getReference("HolidayNames")
                holidaysRef.child(selectedDate).setValue(holidayName).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Holiday saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Write error!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
