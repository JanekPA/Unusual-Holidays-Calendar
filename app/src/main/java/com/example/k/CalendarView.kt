package com.example.k

import android.os.Bundle
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityCalendarViewBinding

class CalendarView : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarViewBinding
    private lateinit var dateTV: TextView
    private lateinit var calendarView: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dateTV = findViewById(R.id.idTVDate)
        calendarView = findViewById(R.id.calendarView)


        calendarView
            .setOnDateChangeListener(
                OnDateChangeListener { view, year, month, dayOfMonth ->

                    val Date = (dayOfMonth.toString() + "-"
                            + (month + 1) + "-" + year)

                    dateTV.setText(Date)
                })

    }
}