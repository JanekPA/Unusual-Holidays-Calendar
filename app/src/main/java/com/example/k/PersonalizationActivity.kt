package com.example.k

import android.content.Context
import android.widget.AutoCompleteTextView
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.k.databinding.ActivityPersonalizationBinding
import com.example.k.models.PersonalizationData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PersonalizationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalizationBinding

    private lateinit var firebaseRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPersonalizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val countries = resources.getStringArray(R.array.countries)
        val activities = resources.getStringArray(R.array.activities)
        val hobbys = resources.getStringArray(R.array.hobbys)
        val arrayCountries = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,countries)
        val arrayActivities = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,activities)
        val arrayHobbys = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hobbys)
        binding.countryAutoComplete.setAdapter(arrayCountries)
        binding.activityAutoComplete.setAdapter(arrayActivities)
        binding.hobbyAutoComplete.setAdapter(arrayHobbys)




        firebaseRef = FirebaseDatabase.getInstance().getReference("UsersPersonalization")

        binding.PersDoneButton.setOnClickListener{
            saveData()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saveData() {
        val chosenCountry = binding.countryAutoComplete.text.toString()
        val chosenActivity = binding.activityAutoComplete.text.toString()
        val chosenHobby = binding.hobbyAutoComplete.text.toString()

            if(chosenCountry.isEmpty() || chosenActivity.isEmpty() || chosenHobby.isEmpty()){
            if (chosenCountry.isEmpty()) binding.countryAutoComplete.error = "Choose a country!"
            if (chosenActivity.isEmpty()) binding.activityAutoComplete.error = "Choose an activity!"
            if (chosenHobby.isEmpty()) binding.hobbyAutoComplete.error = "Choose a hobby!"
                }
            else {
                val sharedPreferences = getSharedPreferences("RegData", Context.MODE_PRIVATE)
                val nickname = sharedPreferences.getString("nickname","")
                val datas = PersonalizationData(
                    nickname,
                    chosenCountry,
                    chosenActivity,
                    chosenHobby
                )
                if (nickname != null) {
                    firebaseRef.child(nickname).setValue(datas)

                        .addOnCompleteListener {
                            Toast.makeText(this, "Data stored successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "error ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                val persDone = Intent(this, MainActivity::class.java)
                startActivity(persDone)
            }
        }
    }


