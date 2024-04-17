package com.example.k

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class EditHolidayActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var holidayNameEditText: EditText
    private lateinit var updateButton: Button
    private fun Int.pad(digits: Int) = toString().padStart(digits, '0')
    private var originalDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_holiday)

        calendarView = findViewById(R.id.calendarView)
        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        updateButton = findViewById(R.id.updateButton)

        originalDate = intent.getStringExtra("date")
        val holidayName = intent.getStringExtra("holidayName")
        holidayNameEditText.setText(holidayName)

        var selectedDate = originalDate

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "${dayOfMonth.pad(2)}-${(month + 1).pad(2)}"
        }

        updateButton.setOnClickListener {
            val newHolidayName = holidayNameEditText.text.toString().trim()

            if (newHolidayName.isEmpty()) {
                Toast.makeText(this, "Holiday name cannot be empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val holidaysRef = database.getReference("HolidayNames")

            originalDate?.let {
                holidaysRef.child(it).removeValue().addOnSuccessListener {
                    holidaysRef.child(selectedDate!!).setValue(newHolidayName).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
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
}