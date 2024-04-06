package com.example.k


import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Spinner
import android.widget.TextView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.k.databinding.ActivityPrefChangingBinding
import com.example.k.models.ListItem
import com.example.k.models.MultiSelectSpinnerAdapter
import com.example.k.models.PersonalizationData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PrefChanging : AppCompatActivity() {
    private lateinit var binding: ActivityPrefChangingBinding
    private var selectedActivity : MutableList<ListItem>? = mutableListOf()
    private var spinnerActivityListItem : ArrayList<ListItem>? = ArrayList()
    private var selectedHobby : MutableList<ListItem> = mutableListOf()
    private var spinnerHobbyListItem : ArrayList<ListItem>? = ArrayList()
    private var spinnerActivity : Spinner ?= null
    private var spinnerHobby : Spinner ?= null
    private var nameActivity : TextView?= null
    private var nameHobby : TextView?= null

    private lateinit var firebaseRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


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
        for(hobby in hobbyArray)
        {
            spinnerHobbyListItem?.add(ListItem(hobby))
        }

        selectedHobby.clear()

        val adapter = MultiSelectSpinnerAdapter(this,
            spinnerActivityListItem!!,
            selectedActivity!!)

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

        val adapter2 = MultiSelectSpinnerAdapter(this,
            spinnerHobbyListItem!!,
            selectedHobby)

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
        binding = ActivityPrefChangingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseRef = FirebaseDatabase.getInstance().getReference("UsersPersonalization")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        val countries = resources.getStringArray(R.array.countries)
        val arrayCountries = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,countries)
        binding.ChangecountryAutoComplete.setAdapter(arrayCountries)
        binding.activitySpinner.setAdapter(adapter)
        binding.hobbySpinner.setAdapter(adapter2)


        binding.PersChangeButton.setOnClickListener {
            updateData()
        }
    }

    private fun updateData() {
        val country = binding.ChangecountryAutoComplete.text.toString()
        val activity = selectedActivity?.map{it.name}
        val hobby = selectedHobby.map{it.name}

        val countries = resources.getStringArray(R.array.countries)

        if (country.isEmpty() || activity!!.isEmpty() || hobby.isEmpty()) {
            if (country.isEmpty()) binding.ChangecountryAutoComplete.error = "Choose a country!"
            if (activity!!.isEmpty()) Toast.makeText(this, "Choose an activity!", Toast.LENGTH_SHORT).show()
            if (hobby.isEmpty()) Toast.makeText(this, "Choose a hobby!", Toast.LENGTH_SHORT).show()
        } else if (!countries.contains(country)) {
            binding.ChangecountryAutoComplete.error = "No country specified in the database!"
        }  else {
            val sharedPreferences = getSharedPreferences("RegData",Context.MODE_PRIVATE)
            val nickname = sharedPreferences.getString("nickname","")
            val datas = PersonalizationData(
                nickname,
                country,
                activity.toString(),
                hobby.toString()
            )
            if (nickname != null) {
                firebaseRef.child(nickname).setValue(datas)
                    .addOnCompleteListener {
                        Toast.makeText(this, "Data changed successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "error ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            val persDone = Intent(this, MainActivity::class.java)
            startActivity(persDone)
        }
    }
    private val Spinner.selectedItems: List<String>
        get() {
            val selectedItems = mutableListOf<String>()
            for(i in 0 until count)
            {
                if(isSelected)
                {
                    selectedItems.add(getItemAtPosition(i).toString())
                }
            }
            return selectedItems
        }

}
