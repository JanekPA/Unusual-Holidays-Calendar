package com.example.k

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityCalendarViewBinding
import com.google.firebase.database.FirebaseDatabase

class CalendarView : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarViewBinding
    private lateinit var dateTV: TextView
    private lateinit var holidayView: TextView
    private lateinit var calendarView: android.widget.CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dateTV = binding.idTVDate
        holidayView = binding.idHolidayView
        calendarView = binding.calendarView

        val database = FirebaseDatabase.getInstance()
        val holidaysRef = database.getReference("HolidayNames")

        val addHolidayButton: Button = findViewById(R.id.addHolidayButton)
        addHolidayButton.setOnClickListener {
            startActivity(Intent(this, AddHolidayActivity::class.java))
        }

        val editHolidayButton: Button = findViewById(R.id.editHolidayButton)
        editHolidayButton.setOnClickListener {
            val dateKey = dateTV.text.toString().substring(0, 5) // Odczytaj klucz "DD-MM"
            holidaysRef.child(dateKey).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val holidayName = dataSnapshot.getValue(String::class.java)
                    val intent = Intent(this, EditHolidayActivity::class.java)
                    intent.putExtra("date", dateKey)
                    intent.putExtra("holidayName", holidayName)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "W tym dniu nie ma święta do edycji!", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        fun Int.pad(digits: Int) = this.toString().padStart(digits, '0')
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dateKey = "${dayOfMonth.pad(2)}-${(month + 1).pad(2)}"
            val fullDate = "$dateKey-$year"

            holidaysRef.child(dateKey).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val holidayName = dataSnapshot.getValue(String::class.java)
                    dateTV.text = fullDate
                    holidayView.text = holidayName ?: "Holiday but no name found"
                } else {
                    dateTV.text = fullDate
                    holidayView.text = "No holiday"
                }
            }
        }
    }
}
