package com.example.k

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.k.databinding.SettingsBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.google.firebase.storage.FirebaseStorage
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.k.databinding.HolidayapprovalBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HolidayApproval : AppCompatActivity() {

    private lateinit var binding: HolidayapprovalBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var profileImageView: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var customsButtonMap: HashMap<String,Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.holidayapproval)
        binding = HolidayapprovalBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setupViews()

        drawerLayout = binding.myDrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        customsButtonMap=HashMap()

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val headerView = navigationView.getHeaderView(0)
        profileImageView = headerView.findViewById(R.id.View_Image2)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_preferences -> {
                    retrievingDataToPrefChanging()
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                }
                R.id.Logout_button -> logout()
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseRef = FirebaseDatabase.getInstance().getReference("HolidayNames")
        checkforApproval()
        listingHolidaystoApprove()
        loadProfilePicture()
    }
    private fun listingHolidaystoApprove() {
        val holidaysRef = firebaseRef
        val holidayNamesLayout: LinearLayout = findViewById(R.id.holidayNamesLayout)

        holidaysRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                holidayNamesLayout.orientation = LinearLayout.VERTICAL
                holidayNamesLayout.removeAllViews()

                for (holidaySnapshot in dataSnapshot.children) {
                    val dateKey = holidaySnapshot.key
                    for (everyHolidaySnapshot in holidaySnapshot.children){
                    val holidayName = everyHolidaySnapshot.key
                    val isAccepted =
                        holidayName?.let {
                            everyHolidaySnapshot.child("isAccepted")
                                .getValue(Boolean::class.java)
                        }

                    Log.e("HOLIDAYDATA", "$isAccepted")

                    if (isAccepted == false) {
                        val customButton =
                            layoutInflater.inflate(R.layout.custom_button_layout, null) as Button

                        customButton.text = "$dateKey"
                        customButton.setOnClickListener {
                            if (dateKey != null) {
                                if (holidayName != null) {
                                    customsButtonMap["$dateKey"] = customButton
                                    showHolidayNameDialog(dateKey, holidayName, customsButtonMap)

                                }
                            }
                        }
                        holidayNamesLayout.addView(customButton)
                    }
                }
            }
                if (holidayNamesLayout.childCount == 0) {
                    val noHolidayButton = Button(this)
                    noHolidayButton.text = "No holidays found"
                    holidayNamesLayout.addView(noHolidayButton)
                }
            } else {
                val noHolidayButton = Button(this)
                noHolidayButton.text = "No holidays found XD"
                holidayNamesLayout.removeAllViews()
                holidayNamesLayout.addView(noHolidayButton)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching holiday details.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkforApproval() {
        Log.d("HolidayApproval", "check for Approval called")
        firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("HolidayApproval", "onDataChange called")
                for (holidaySnapshot in dataSnapshot.children) {
                    val isAccepted = holidaySnapshot.child("isAccepted").getValue(Boolean::class.java)
                    if (isAccepted == false) {
                        // Log a message to Logcat
                        Log.e("HolidayApproval", "Holiday pending approval: ${holidaySnapshot.key}")
                        // Update the UI or perform any necessary actions here
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("HolidayApproval", "onCancelled called with error: ${databaseError.message}")
                Toast.makeText(this@HolidayApproval, "Failed to check approvals: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun retrievingDataToPrefChanging()
    {
        val firebaseAuthRD = FirebaseAuth.getInstance()
        val firebaseUserRD = firebaseAuthRD.currentUser
        val firebaseDatabaseRD = FirebaseDatabase.getInstance()
        val userPersonalizationRD = firebaseDatabaseRD.getReference("UsersPersonalization")
        firebaseUserRD?.let { user ->
            val uid = user.uid

            userPersonalizationRD.child(uid).get().addOnSuccessListener { persSnapshot ->
                if(persSnapshot.exists())
                {
                    val countryName = persSnapshot.child("Country").children.first().key
                    ///AKTYWNOSC + HOBBY - POBRANIE

                    ///AKTYWNOSC + HOBBY - POBRANIE
                    val intent = Intent(this, PrefChanging::class.java)
                    intent.putExtra("countryName", countryName)
                    startActivity(intent)
                }
            }

        }
    }

    private fun setupViews() {
        binding.AccountButton?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.CalendarVOptions.setOnClickListener {
            val intent = Intent(this, CalendarView::class.java)
            startActivity(intent)
        }
        binding.AddButtonOptions?.setOnClickListener {
            val intent = Intent(this, AddHolidayActivity::class.java)
            startActivity(intent)
        }
        binding.CommunityOptions?.setOnClickListener {
            val intent = Intent(this, Community::class.java)
            startActivity(intent)
        }
        binding.button4Options?.setOnClickListener {
            val intent = Intent(this, AddNotes::class.java)
            startActivity(intent)
        }

        drawerLayout = binding.myDrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun loadProfilePicture() {
        // How is this working? idk trust me bro
        // Code bellow made especially for nav_drawer
        val currentUser: FirebaseUser? = firebaseAuth.currentUser
        currentUser?.let {
            val uid = it.uid
            val profilePicturesRef = FirebaseStorage.getInstance().reference.child("ProfilePictures/$uid/")
            val filename = "profile.jpg"
            val imageRef = profilePicturesRef.child(filename)

            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this@HolidayApproval)
                    .load(uri)
                    .into(profileImageView)
            }.addOnFailureListener { exception ->
                // errors
                Log.e("MainActivity", "Error loading profile picture from Firebase Storage: $exception")
            }
        }
    }

    private fun logout() {
        firebaseAuth.signOut()
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("remember", "false")
        editor.apply()
        finish()
        Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
    }
    private fun showHolidayNameDialog(dateKey: String, holidayName: String, customsButtonMap: Map<String, Button>) {
        val dialogView = layoutInflater.inflate(R.layout.holiday_dialog_admin, null)
        val holidayNameTextView = dialogView.findViewById<TextView>(R.id.holidayNameTextView)
        holidayNameTextView.text = holidayName

        val holidaysRef = FirebaseDatabase.getInstance().getReference("HolidayNames")
        val acceptButton = dialogView.findViewById<Button>(R.id.acceptHoliday)
        val rejectButton = dialogView.findViewById<Button>(R.id.rejectHoliday)
        /*val editButton = dialogView.findViewById<Button>(R.id.editHoly)
        editButton.setOnClickListener {

            val firebaseAuth = FirebaseAuth.getInstance()
            val firebaseUser = firebaseAuth.currentUser
            firebaseUser?.let { user ->
                val uid = user.uid
                Log.e("HOLIDAYAPPROVAL","$dateKey, $holidayName")
                holidaysRef.child(dateKey).child(holidayName).get()
                    .addOnSuccessListener { holidaySnapshot ->
                        Log.d("HolidayApproval", "Holiday snapshot value: $holidaySnapshot")
                        if (holidaySnapshot.exists()) {
                            val countryName = holidaySnapshot.child("Country").children.first().key
                            val holidayAuthor = holidaySnapshot.child("uid").getValue(String::class.java)
                            if(holidayAuthor == uid) {
                                val intent = Intent(this, EditHolidayActivity::class.java)
                                intent.putExtra("dateKey", dateKey)
                                intent.putExtra("holidayName", holidayName)
                                intent.putExtra("country", countryName)

                                startActivity(intent)
                                (it.context as? AlertDialog)?.dismiss()
                            }
                            else
                            {
                                Toast.makeText(
                                    this,
                                    "You are not author, you can not modify!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }*/

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val holidayNamesLayout: LinearLayout = findViewById(R.id.holidayNamesLayout)
        val dialog = builder.create()

        dialog.show()
        Log.e("HOLIDAYAPPROVAL","$dateKey, $holidayName")
        holidaysRef.child(dateKey).child(holidayName).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                Log.d("HolidayApproval", "Data snapshot value: $dataSnapshot")

                val country = dataSnapshot.child("Country").children.map { activity ->
                    activity.key!!
                }.toList()


                val activities = dataSnapshot.child("Activities").children.map { activity ->
                    activity.key!!
                }.toList()

                val hobbies = dataSnapshot.child("Hobbies").children.map { hobby ->
                    hobby.key!!
                }.toList()

                val holidayInfo = StringBuilder()
                val countryString = country.joinToString(", ")
                holidayInfo.append("Country: $countryString\n")
                holidayInfo.append("Date: $dateKey\n\n")

                holidayInfo.append("Activities:\n")
                activities.forEach { activity ->
                    holidayInfo.append("- $activity\n")
                }

                holidayInfo.append("\nHobbies:\n")
                hobbies.forEach { hobby ->
                    holidayInfo.append("- $hobby\n")
                }

                val mainWindowTextView = dialogView.findViewById<TextView>(R.id.MainWindow)
                mainWindowTextView.text = holidayInfo.toString()
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting data", exception)
        }

        val customButton = customsButtonMap[dateKey]
        // Set onClick listeners for accept and reject buttons
        acceptButton.setOnClickListener{
            // Update isAccepted to true in the database
            holidaysRef.child(dateKey).child(holidayName).child("isAccepted").setValue(true)
                .addOnSuccessListener {
                    // Dismiss the dialog after successful update
                    dialog.dismiss()
                    holidayNamesLayout.removeView(customButton)
                }
        }

        rejectButton.setOnClickListener{
            // Update isAccepted to false in the database
            holidaysRef.child(dateKey).child(holidayName).child("isAccepted").setValue(false)
                .addOnSuccessListener {
                    // Dismiss the dialog after successful update
                    dialog.dismiss()
                    holidayNamesLayout.removeView(customButton)
                }
        }
    }
}