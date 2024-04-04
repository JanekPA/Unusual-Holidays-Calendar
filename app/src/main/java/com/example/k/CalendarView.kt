package com.example.k

import android.os.Bundle
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.k.databinding.ActivityCalendarViewBinding

class CalendarView : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarViewBinding
    private lateinit var dateTV: TextView
    private lateinit var calendarView: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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