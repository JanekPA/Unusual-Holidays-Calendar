package com.example.k

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityEditHolidayBinding
import com.example.k.models.ListItem
import com.example.k.models.MultiSelectSpinnerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditHolidayActivity : AppCompatActivity() {
    private lateinit var holidayNameEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var binding: ActivityEditHolidayBinding
    private var selectedActivity: MutableList<ListItem>? = mutableListOf()
    private var spinnerActivityListItem: ArrayList<ListItem>? = ArrayList()
    private var selectedHobby: MutableList<ListItem> = mutableListOf()
    private var spinnerHobbyListItem: ArrayList<ListItem>? = ArrayList()
    private var countryListItem: ArrayList<ListItem>? = ArrayList()
    private var spinnerActivity: Spinner? = null
    private var spinnerHobby: Spinner? = null
    private var nameActivity: TextView? = null
    private var nameHobby: TextView? = null
    private var dateKey: String? = null
    private var holidayName: String? = null
    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditHolidayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        holidayNameEditText = findViewById(R.id.holidayNameEditText)
        updateButton = findViewById(R.id.updateButton)
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

        val countries = resources.getStringArray(R.array.countries)
        val countryArray =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries)



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

        /*originalDate = intent.getStringExtra("date")
        val holidayName = intent.getStringExtra("holidayName")
        holidayNameEditText.setText(holidayName)

        selectedDate = ""

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "${dayOfMonth.toString().padStart(2, '0')}-${
                (month + 1).toString().padStart(2, '0')
            }"
        }*/

        dateKey = intent.getStringExtra("dateKey")
        holidayName = intent.getStringExtra("holidayName")
        holidayNameEditText.setText(holidayName)

        binding.countryAutoComplete.setAdapter(countryArray)
        binding.activitySpinner.setAdapter(adapter)
        binding.hobbySpinner.setAdapter(adapter2)

        binding.updateButton.setOnClickListener {
            updateData()
        }

    }


    private fun updateData() {
        val country = binding.countryAutoComplete.text.toString()
        val activity = selectedActivity?.map { it.name to (it.itemId % 15)+1}?.toMap()
        val hobby = selectedHobby.map { it.name to (it.itemId % 15)+1 }.toMap()

        val countries = resources.getStringArray(R.array.countries)

        val newHolidayName = holidayNameEditText.text.toString().trim()
        val oldHolidayName = holidayName.toString()

        if (newHolidayName.isEmpty() || dateKey==null || country.isEmpty() || activity.isNullOrEmpty() || hobby.isNullOrEmpty()) {
            if (newHolidayName.isEmpty())
                Toast.makeText(this, "Holiday name cannot be empty!", Toast.LENGTH_LONG).show()
            if (dateKey==null)
                Toast.makeText(this, "Please select a date!", Toast.LENGTH_LONG).show()
            if (country.isEmpty()) binding.countryAutoComplete.error = "Choose a country!"
            if (activity.isNullOrEmpty()) Toast.makeText(
                this,
                "Choose an activity!",
                Toast.LENGTH_SHORT
            ).show()
            if (hobby.isNullOrEmpty()) Toast.makeText(
                this,
                "Choose a hobby!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (!countries.contains(country)) {
            binding.countryAutoComplete.error = "No country specified in the database!"

        } else {
            val database = FirebaseDatabase.getInstance()
            val holidaysRef = database.getReference("HolidayNames")

            val firebaseReff = FirebaseDatabase.getInstance().getReference("HolidayNames").child(
                dateKey.toString()
            )
            if (oldHolidayName != null) {
                firebaseReff.child(oldHolidayName).removeValue().addOnCompleteListener {

                    val firebaseAuth = FirebaseAuth.getInstance()
                    val firebaseUser = firebaseAuth.currentUser
                    firebaseUser?.let { user ->
                        val uid = user.uid

                        val firebaseRef = FirebaseDatabase.getInstance().getReference("HolidayNames").child(dateKey.toString())
                        firebaseRef.child(newHolidayName).child("uid").setValue(uid)

                        val countryData = countries.indexOf(country) + 1

                        firebaseRef.child(newHolidayName).child("Country").child(country)
                            .setValue(countryData)
                        firebaseRef.child(newHolidayName).child("Activities").setValue(activity)
                        firebaseRef.child(newHolidayName).child("Hobbies").setValue(hobby)
                        firebaseRef.child(newHolidayName).child("name").setValue(newHolidayName)
                            .addOnCompleteListener {
                                Toast.makeText(
                                    this,
                                    "Data updated successfully!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                finish()
                                startActivity(
                                    Intent(
                                        this@EditHolidayActivity,
                                        MainActivity::class.java
                                    )
                                )
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    this,
                                    "Error: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                    }
                }
            }
        }
    }
}
