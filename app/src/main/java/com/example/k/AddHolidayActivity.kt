package com.example.k

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddHolidayActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var holidayNameEditText: EditText
    private lateinit var countryAutoComplete: AutoCompleteTextView
    private lateinit var hobbiesButton: Button
    private lateinit var activitiesButton: Button

    private var selectedHobbies = mutableListOf<String>()
    private var selectedActivities = mutableListOf<String>()
    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_holiday)

        calendarView = findViewById(R.id.calendarV)
        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        countryAutoComplete = findViewById(R.id.countryAutoComplete)
        hobbiesButton = findViewById(R.id.hobbiesButton)
        activitiesButton = findViewById(R.id.activitiesButton)

        val countriesAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.countries)
        )
        countryAutoComplete.setAdapter(countriesAdapter)

        selectedDate = ""  // Initialize with an empty string

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "${dayOfMonth.toString().padStart(2, '0')}-${
                (month + 1).toString().padStart(2, '0')
            }"
        }

        val hobbies = resources.getStringArray(R.array.hobbys)
        val activities = resources.getStringArray(R.array.activities)
        val selectedHobbiesFlags = BooleanArray(hobbies.size)
        val selectedActivitiesFlags = BooleanArray(activities.size)

        hobbiesButton.setOnClickListener {
            showMultiSelectDialog(hobbies, selectedHobbiesFlags, "Select Hobbies") { selected ->
                selectedHobbies = selected.toMutableList()
            }
        }

        activitiesButton.setOnClickListener {
            showMultiSelectDialog(activities, selectedActivitiesFlags, "Select Activities") { selected ->
                selectedActivities = selected.toMutableList()
            }
        }

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveHolidayDetails()
        }
    }

    private fun showMultiSelectDialog(items: Array<String>, selectedItems: BooleanArray, title: String, onSelectionChanged: (selected: List<String>) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMultiChoiceItems(items, selectedItems) { _, which, isChecked ->
            selectedItems[which] = isChecked
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            val selectedStrings = items.filterIndexed { index, _ -> selectedItems[index] }
            onSelectionChanged(selectedStrings)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun saveHolidayDetails() {
        val holidayName = holidayNameEditText.text.toString().trim()
        val selectedCountry = countryAutoComplete.text.toString()

        if (holidayName.isEmpty()) {
            Toast.makeText(this, "Holiday name cannot be empty!", Toast.LENGTH_LONG).show()
            return
        } else if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date!", Toast.LENGTH_LONG).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val holidayDetailsRef = database.getReference("HolidayNames").child(selectedDate)

        val holidayDetails = mapOf(
            "name" to holidayName,
            "country" to selectedCountry,
            "hobbies" to selectedHobbies.joinToString(", "),
            "activities" to selectedActivities.joinToString(", ")
        )

        holidayDetailsRef.setValue(holidayDetails).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Holiday details saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Write error!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}