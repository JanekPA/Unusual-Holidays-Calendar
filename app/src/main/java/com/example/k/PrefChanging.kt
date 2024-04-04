package com.example.k

import android.content.Context
import android.content.Intent
import android.view.View
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.k.databinding.ActivityPrefChangingBinding
import com.example.k.models.PersonalizationData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PrefChanging : AppCompatActivity() {
    private lateinit var binding: ActivityPrefChangingBinding

    private lateinit var firebaseRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrefChangingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseRef = FirebaseDatabase.getInstance().getReference("UsersPersonalization")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val countries = resources.getStringArray(R.array.countries)
        val activities = resources.getStringArray(R.array.activities)
        val hobbys = resources.getStringArray(R.array.hobbys)
        val arrayCountries = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,countries)
        val arrayActivities = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,activities)
        val arrayHobbys = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hobbys)
        binding.ChangecountryAutoComplete.setAdapter(arrayCountries)
        binding.ChangeactivityAutoComplete.setAdapter(arrayActivities)
        binding.ChangehobbyAutoComplete.setAdapter(arrayHobbys)
        binding.PersChangeButton.setOnClickListener {
            updateData()
        }
    }

    private fun updateData() {
        val country = binding.ChangecountryAutoComplete.text.toString()
        val activity = binding.ChangeactivityAutoComplete.text.toString()
        val hobby = binding.ChangehobbyAutoComplete.text.toString()

        val countries = resources.getStringArray(R.array.countries)
        val activities = resources.getStringArray(R.array.activities)
        val hobbys = resources.getStringArray(R.array.hobbys)

        if (country.isEmpty() || activity.isEmpty() || hobby.isEmpty()) {
            if (country.isEmpty()) binding.ChangecountryAutoComplete.error = "Choose a country!"
            if (activity.isEmpty()) binding.ChangeactivityAutoComplete.error = "Choose an activity!"
            if (hobby.isEmpty()) binding.ChangehobbyAutoComplete.error = "Choose a hobby!"
        } else if (!countries.contains(country)) {
            binding.ChangecountryAutoComplete.error = "No country specified in the database!"
        } else if (!activities.contains(activity)) {
            binding.ChangeactivityAutoComplete.error = "No activity specified in the database!"
        } else if (!hobbys.contains(hobby)) {
            binding.ChangehobbyAutoComplete.error = "No hobby specified in the database!"
        } else {
            val sharedPreferences = getSharedPreferences("RegData",Context.MODE_PRIVATE)
            val nickname = sharedPreferences.getString("nickname","")
            val datas = PersonalizationData(
                nickname,
                country,
                activity,
                hobby
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
}
