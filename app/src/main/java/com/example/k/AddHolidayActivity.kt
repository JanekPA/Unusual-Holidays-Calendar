package com.example.k

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityAddHolidayBinding
import com.example.k.models.ListItem
import com.example.k.models.MultiSelectSpinnerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddHolidayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHolidayBinding
    private lateinit var calendarView: CalendarView
    private lateinit var holidayNameEditText: EditText
    private lateinit var countryAutoComplete: AutoCompleteTextView
    private var selectedActivity: MutableList<ListItem>? = mutableListOf()
    private var spinnerActivityListItem: ArrayList<ListItem>? = ArrayList()
    private var selectedHobby: MutableList<ListItem> = mutableListOf()
    private var spinnerHobbyListItem: ArrayList<ListItem>? = ArrayList()
    private var spinnerActivity: Spinner? = null
    private var spinnerHobby: Spinner? = null
    private var nameActivity: TextView? = null
    private var nameHobby: TextView? = null
    private lateinit var selectedDate: String
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddHolidayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendarView = findViewById(R.id.calendarV)
        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        countryAutoComplete = findViewById(R.id.countryAutoComplete)
        spinnerActivity = findViewById(R.id.activitySpinner)
        spinnerHobby = findViewById(R.id.hobbySpinner)
        nameActivity = findViewById(R.id.activityHead)
        nameHobby = findViewById(R.id.hobbyHead)

        val activitiesArray = resources.getStringArray(R.array.activities)
        for (activity in activitiesArray) {
            spinnerActivityListItem?.add(ListItem(activity))
        }

        selectedActivity!!.clear()

        val hobbyArray = resources.getStringArray(R.array.hobbys)
        for (hobby in hobbyArray) {
            spinnerHobbyListItem?.add(ListItem(hobby))
        }

        selectedHobby.clear()

        val adapter = MultiSelectSpinnerAdapter(
            this,
            spinnerActivityListItem!!,
            selectedActivity!!
        )

        spinnerActivity?.adapter = adapter

        adapter.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<ListItem>,
                pos: Int,
            ) {
                nameActivity?.text = "Activity"
                Log.e("getSelectedItems", selectedItems.toString())
                Log.e("getSelectedItems", selectedItems.size.toString())
            }
        }
        )

        val adapter2 = MultiSelectSpinnerAdapter(
            this,
            spinnerHobbyListItem!!,
            selectedHobby
        )

        spinnerHobby?.adapter = adapter2

        adapter2.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<ListItem>,
                pos: Int,
            ) {
                nameHobby?.text = "Hobby"

                Log.e("getSelectedItems", selectedItems.toString())
                Log.e("getSelectedItems", selectedItems.size.toString())
            }
        }
        )

        val countries = resources.getStringArray(R.array.countries)
        val arrayCountries = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            countries
        )

        binding.countryAutoComplete.setAdapter(arrayCountries)
        binding.activitySpinner.setAdapter(adapter)
        binding.hobbySpinner.setAdapter(adapter2)

        selectedDate = ""

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "${dayOfMonth.toString().padStart(2, '0')}-${
                (month + 1).toString().padStart(2, '0')
            }"
        }

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveHolidayDetails()
        }
    }


    private fun saveHolidayDetails() {
        val country = binding.countryAutoComplete.text.toString()
        val activity = selectedActivity?.map { it.name  to (it.itemId % 15)+1}?.toMap()
        val hobby = selectedHobby.map { it.name to (it.itemId % 15)+1 }.toMap()

        val countries = resources.getStringArray(R.array.countries)

        val holidayName = holidayNameEditText.text.toString().trim()
        if (holidayName.isEmpty() || selectedDate.isEmpty() || country.isEmpty() || activity.isNullOrEmpty() || hobby.isNullOrEmpty()) {
            if (holidayName.isEmpty())
                Toast.makeText(this, "Holiday name cannot be empty!", Toast.LENGTH_LONG).show()
            if (selectedDate.isEmpty())
                Toast.makeText(this, "Please select a date!", Toast.LENGTH_LONG).show()
            if (country.isEmpty()) binding.countryAutoComplete.error = "Choose a country!"
            if (activity.isNullOrEmpty()) Toast.makeText(
                this,
                "Choose an activity!",
                Toast.LENGTH_SHORT).show()
            if (hobby.isNullOrEmpty()) Toast.makeText(
                this,
                "Choose a hobby!",
                Toast.LENGTH_SHORT).show()
        } else if (!countries.contains(country)) {
            binding.countryAutoComplete.error = "No country specified in the database!"
        } else {
            val firebaseRef = FirebaseDatabase.getInstance().getReference("HolidayNames").child(selectedDate)
            val countryData = countries.indexOf(country)+1

            firebaseRef.child(holidayName).child("Country").child(country).setValue(countryData)
            firebaseRef.child(holidayName).child("Activities").setValue(activity)
            firebaseRef.child(holidayName).child("Hobbies").setValue(hobby)
            firebaseRef.child(holidayName).child("name").setValue(holidayName)
                .addOnCompleteListener {
                    Toast.makeText(this, "Data add successfully!", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
}
