package com.example.k

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

        // Konfiguracja UI...
        dateTV = binding.idTVDate
        holidayView=binding.idHolidayView
        calendarView = binding.calendarView

        // Inicjalizacja Firebase Database
        val database = FirebaseDatabase.getInstance()
        val holidaysRef = database.getReference("HolidayNames")

        val addHolidayButton: Button = findViewById(R.id.addHolidayButton)
        addHolidayButton.setOnClickListener {
            val intent = Intent(this, AddHolidayActivity::class.java)
            startActivity(intent)
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dateKey = "$dayOfMonth-${month + 1}-$year"
            holidaysRef.child(dateKey).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val holidayName = dataSnapshot.getValue(String::class.java)
                    dateTV.text = dateKey
                    holidayView.text = "$holidayName"
                } else {
                    dateTV.text = dateKey
                    holidayView.text = "No holiday"
                }
            }
        }
    }
}