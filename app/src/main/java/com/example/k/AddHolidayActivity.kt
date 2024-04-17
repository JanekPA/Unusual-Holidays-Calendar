package com.example.k

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.selects.select

class AddHolidayActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var holidayNameEditText: EditText


    private fun Int.pad(digits: Int) = toString().padStart(digits, '0')

    private lateinit var holidaysRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_holiday)
        holidaysRef = FirebaseDatabase.getInstance().getReference("HolidayNames")

        calendarView = findViewById(R.id.calendarV)
        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        var selectedDate = ""

        calendarView.setOnDateChangeListener { _, _, month, dayOfMonth ->
            val formattedDate = "${dayOfMonth.pad(2)}-${(month + 1).pad(2)}"
            selectedDate = formattedDate
            Log.e("SelectedDate", "New selected date: $selectedDate")
        }

        saveButton.setOnClickListener {
            val holidayName = holidayNameEditText.text.toString().trim()

            if (holidayName.isEmpty()) {
                Toast.makeText(this, "Holiday name cannot be empty!", Toast.LENGTH_LONG).show()
            } else if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date!", Toast.LENGTH_LONG).show()
            } else {
                val holidaysRef = FirebaseDatabase.getInstance().getReference("HolidayNames")
                holidaysRef.child(selectedDate).setValue(holidayName).addOnCompleteListener {task->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Holiday saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Write error!", Toast.LENGTH_SHORT).show()
                        Log.e("Firebase", "Error writing to database", task.exception)
                    }
                }
            }
        }
    }
}
