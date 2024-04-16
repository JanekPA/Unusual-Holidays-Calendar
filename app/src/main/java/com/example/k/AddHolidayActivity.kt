package com.example.k

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddHolidayActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var holidayNameEditText: EditText
    private lateinit var countryAutoComplete: AutoCompleteTextView
    private lateinit var hobbySpinner: Spinner
    private lateinit var activitySpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_holiday)

        calendarView = findViewById(R.id.calendarV)
        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        countryAutoComplete = findViewById(R.id.countryAutoComplete)
        hobbySpinner = findViewById(R.id.hobbySpinner)
        activitySpinner = findViewById(R.id.activitySpinner)

        val countriesAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.countries)
        )
        countryAutoComplete.setAdapter(countriesAdapter)

        var selectedDate =
            ""  // Inicjalizacja zmiennej poza listenerem, aby była dostępna globalnie w klasie

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Formatuj datę do formatu DD-MM
            selectedDate = "${dayOfMonth.toString().padStart(2, '0')}-${
                (month + 1).toString().padStart(2, '0')
            }"
        }

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveHolidayDetails(selectedDate)  // Przekazuj wybraną datę do funkcji zapisującej
        }
    }

    private fun saveHolidayDetails(selectedDate: String) {
        val holidayName = holidayNameEditText.text.toString().trim()
        val selectedCountry = countryAutoComplete.text.toString()
        val selectedHobbies = hobbySpinner.selectedItem.toString()
        val selectedActivities = activitySpinner.selectedItem.toString()

        if (holidayName.isEmpty()) {
            Toast.makeText(this, "Holiday name cannot be empty!", Toast.LENGTH_LONG).show()
        } else if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date!", Toast.LENGTH_LONG).show()
        } else {
            val database = FirebaseDatabase.getInstance()
            val holidayDetailsRef = database.getReference("HolidaysDetails").child(selectedDate)

            val holidayDetails = mapOf(
                "name" to holidayName,
                "country" to selectedCountry,
                "hobbies" to selectedHobbies,
                "activities" to selectedActivities
            )

            holidayDetailsRef.setValue(holidayDetails).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Holiday details saved!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Write error!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}