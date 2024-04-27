package com.example.k

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.k.databinding.ActivityCalendarViewBinding
import com.google.firebase.database.FirebaseDatabase
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage


class CalendarView : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarViewBinding
    private lateinit var dateTV: TextView
    private lateinit var holidayView: TextView
    private lateinit var calendarView: android.widget.CalendarView
    private lateinit var holidayNamesLayout: LinearLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var profileImageView: ImageView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dateTV = binding.idTVDate
        holidayView = binding.idHolidayView
        calendarView = binding.calendarView
        holidayNamesLayout = binding.holidayNamesLayout
        setupViews()
        drawerLayout = binding.myDrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val headerView = navigationView.getHeaderView(0)
        profileImageView = headerView.findViewById(R.id.View_Image2)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_preferences -> {
                    val intent = Intent(this, PrefChanging::class.java)
                    startActivity(intent)
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
        loadProfilePicture()

        // Check for specific email

        val database = FirebaseDatabase.getInstance()
        val holidaysRef = database.getReference("HolidayNames")

//        val addHolidayButton: Button = findViewById(R.id.addHolidayButton)
//        addHolidayButton.setOnClickListener {
//            startActivity(Intent(this, AddHolidayActivity::class.java))
//        }
//
//        val editHolidayButton: Button = findViewById(R.id.editHolidayButton)
//        editHolidayButton.setOnClickListener {
//            val dateKey = dateTV.text.toString().substring(0, 5) // Odczytaj klucz "DD-MM"
//            holidaysRef.child(dateKey).get().addOnSuccessListener { dataSnapshot ->
//                if (dataSnapshot.exists()) {
//                    dataSnapshot.children.forEach { holidaySnapshot ->
//                        val holidayName = holidaySnapshot.child("name").getValue(String::class.java)
//                        if (holidayName != null) { // Jeżeli nazwa święta istnieje
//                            showHolidayNameDialog(holidayName)
//                            return@addOnSuccessListener // Wychodzi po znalezieniu pierwszego święta
//                        }
//                    }
//                    // Jeśli pętla się zakończyła i nie znaleziono święta, to wyświetla komunikat
//                    Toast.makeText(this, "W tym dniu nie ma święta do edycji!", Toast.LENGTH_LONG).show()
//                } else {
//                    Toast.makeText(this, "Nie znaleziono danych dla tego dnia.", Toast.LENGTH_LONG).show()
//                }
//            }.addOnFailureListener {
//                // Obsługa błędu pobierania danych, np. brak połączenia internetowego
//                Toast.makeText(this, "Nie udało się pobrać danych.", Toast.LENGTH_LONG).show()
//            }
//        }

        fun Int.pad(digits: Int) = this.toString().padStart(digits, '0')
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dateKey = "${dayOfMonth.toString().padStart(2, '0')}-${(month + 1).toString().padStart(2, '0')}"
            val fullDate = "$dateKey-$year"
            holidaysRef.child(dateKey).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    holidayNamesLayout.orientation = LinearLayout.VERTICAL
                    holidayNamesLayout.removeAllViews()

                    for (holidaySnapshot in dataSnapshot.children) {
                        val holidayName = holidaySnapshot.child("name").getValue(String::class.java)
                        holidayName?.let { name ->
                            val customButton = layoutInflater.inflate(R.layout.custom_button_layout, null) as Button
                            customButton.text = name
                            customButton.setOnClickListener {
                                showHolidayNameDialog(name)
                            }
                            holidayNamesLayout.addView(customButton)
                        }
                    }

                    if (holidayNamesLayout.childCount == 0) {
                        val noHolidayButton = Button(this)
                        noHolidayButton.text = "No holidays found"
                        holidayNamesLayout.addView(noHolidayButton)
                    }

                    dateTV.text = fullDate
                } else {
                    val noHolidayButton = Button(this)
                    noHolidayButton.text = "No holiday"
                    holidayNamesLayout.removeAllViews()
                    holidayNamesLayout.addView(noHolidayButton)
                    dateTV.text = fullDate
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error fetching holiday details.", Toast.LENGTH_SHORT).show()
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
                Glide.with(this@CalendarView)
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
    private fun showHolidayNameDialog(holidayName: String) {
        val dialogView = layoutInflater.inflate(R.layout.holiday_dialog, null)
        val holidayNameTextView = dialogView.findViewById<TextView>(R.id.holidayNameTextView)
        holidayNameTextView.text = holidayName

        val editButton = dialogView.findViewById<Button>(R.id.editHoly)
        editButton.setOnClickListener {
            val intent = Intent(this, EditHolidayActivity::class.java)
            intent.putExtra("holidayName", holidayName)
            startActivity(intent)
            (it.context as? AlertDialog)?.dismiss()
        }

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        dialog.show()

        val database = FirebaseDatabase.getInstance()
        val holidaysRef = database.getReference("HolidayNames")
        val fullDate = dateTV.text.toString()
        val dateKey = fullDate.substring(0, 5)

        holidaysRef.child(dateKey).child(holidayName).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

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
                country?.let { holidayInfo.append("Country: $it\n") }
                holidayInfo.append("Date: $fullDate\n\n")

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
    }
}
