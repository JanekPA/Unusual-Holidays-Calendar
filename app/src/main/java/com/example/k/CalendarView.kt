package com.example.k

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.k.databinding.ActivityCalendarViewBinding
import com.example.k.models.ListItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var uid: String

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
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        uid = firebaseAuth.currentUser?.uid.toString()
        loadNickname()
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

        loadProfilePicture()

        // Check for specific email

        val holidaysRef = firebaseDatabase.getReference("HolidayNames")



        fun Int.pad(digits: Int) = this.toString().padStart(digits, '0')
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dateKey = "${dayOfMonth.toString().padStart(2, '0')}-${(month + 1).toString().padStart(2, '0')}"
            val fullDate = "$dateKey-$year"

            val firebaseUserRD = firebaseAuth.currentUser
            val userPersonalizationRD =
                firebaseDatabase.getReference("UsersPersonalization")
            val activityListP = mutableListOf<ListItem>()
            val hobbyListP = mutableListOf<ListItem>()
            firebaseUserRD?.let { user ->
                val uid = user.uid

                userPersonalizationRD.child(uid).get()
                    .addOnSuccessListener { persSnapshot ->
                        if (persSnapshot.exists()) {
                            //val countryName =
                            //    persSnapshot.child("Country").children.first().key
                            val hobbiesP = persSnapshot.child("Hobbies")
                            val activitiesP = persSnapshot.child("Activities")
                            ///AKTYWNOSC + HOBBY - POBRANIE
                            for (hobbySnapshot in hobbiesP.children) {
                                val hobbyName = hobbySnapshot.key
                                hobbyName?.let {
                                    hobbyListP.add(ListItem(it))
                                }
                            }

                            ///AKTYWNOSC + HOBBY - POBRANIE
                            for (activitySnapshot in activitiesP.children) {
                                val activityName = activitySnapshot.key
                                activityName?.let {
                                    activityListP.add(ListItem(it))
                                }
                            }
                        }
                    }
            }

            holidaysRef.child(dateKey).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    holidayNamesLayout.orientation = LinearLayout.VERTICAL
                    holidayNamesLayout.removeAllViews()

                    for (holidaySnapshot in dataSnapshot.children) {

                        var check1 = 0;
                        var check2 =0;
                        var check3 = 0; //for approval
                        var check4 = 0; //for rejection

                        val hobbiesC = holidaySnapshot.child("Hobbies")
                        val activitiesC = holidaySnapshot.child("Activities")
                        val approvalC = holidaySnapshot.child("isAccepted")
                        val rejectionC = holidaySnapshot.child("isRejected")
                        ///AKTYWNOSC + HOBBY - POBRANIE
                        val hobbyListC = mutableListOf<ListItem>()
                        for (hobbySnapshot in hobbiesC.children) {
                            val hobbyName = hobbySnapshot.key
                            hobbyName?.let { hobbyListC.add(ListItem(it)) }
                        }

                        ///AKTYWNOSC + HOBBY - POBRANIE
                        val activityListC = mutableListOf<ListItem>()
                        for (activitySnapshot in activitiesC.children) {
                            val activityName = activitySnapshot.key
                            activityName?.let { activityListC.add(ListItem(it)) }
                        }

                        for (activityC in activityListC) {
                            if (activityListP.any { activityP -> activityP.name == activityC.name }) {
                                check1 = 1
                                break;
                            }
                        }

                        for (hobbyC in hobbyListC) {
                            if (hobbyListP.any { hobbyP -> hobbyP.name == hobbyC.name }) {
                                check2 = 1
                                break;
                            }
                        }
                        if(approvalC.value == true)
                        {
                            check3 = 1;
                        }
                        if(rejectionC.value == true)
                        {
                            check4 = 1;
                        }
                        val author = holidaySnapshot.child("uid").getValue(String::class.java)
                        firebaseUserRD?.let { user ->
                            val userid = user.uid


                            if (((check1 == 1 || check2 == 1) && check3 == 1 && check4 == 0) || author == userid) {
                                val holidayName =
                                    holidaySnapshot.child("name").getValue(String::class.java)
                                holidayName?.let { name ->
                                    val customButton = layoutInflater.inflate(
                                        R.layout.custom_button_layout,
                                        null
                                    ) as Button
                                    customButton.text = name
                                    customButton.setOnClickListener {
                                        showHolidayNameDialog(name)
                                    }
                                    holidayNamesLayout.addView(customButton)
                                }
                            }
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
    private fun showHolidayNameDialog(holidayName: String) {
        val dialogView = layoutInflater.inflate(R.layout.holiday_dialog, null)
        val holidayNameTextView = dialogView.findViewById<TextView>(R.id.holidayNameTextView)
        holidayNameTextView.text = holidayName

        val holidaysRef = firebaseDatabase.getReference("HolidayNames")
        val fullDate = dateTV.text.toString()
        val dateKey = fullDate.substring(0, 5)

        val editButton = dialogView.findViewById<Button>(R.id.editHoly)

        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            holidaysRef.child(dateKey).child(holidayName).child("uid").get().addOnSuccessListener { dataSnapshot ->
                val authorUID = dataSnapshot.getValue(String::class.java)
                if (authorUID == uid) {
                    editButton.visibility = View.VISIBLE
                } else {
                    editButton.visibility = View.GONE
                }
            }.addOnFailureListener {
                Log.e("Firebase", "Error fetching author UID", it)
            }
        }

        editButton.setOnClickListener {

            val firebaseUser = firebaseAuth.currentUser
            firebaseUser?.let { user ->
                val uid = user.uid

                holidaysRef.child(dateKey).child(holidayName).get()
                    .addOnSuccessListener { holidaySnapshot ->
                        if (holidaySnapshot.exists()) {
                            val description = holidaySnapshot.child("description").getValue(String::class.java)
                            val countryName = holidaySnapshot.child("Country").children.first().key
                            val holidayAuthor = holidaySnapshot.child("uid").getValue(String::class.java)
                            val hobbies = holidaySnapshot.child("Hobbies")
                            val activities = holidaySnapshot.child("Activities")
                            if (holidayAuthor == uid) {
                                val intent = Intent(this, EditHolidayActivity::class.java)
                                val activityList = mutableListOf<ListItem>()
                                val hobbyList = mutableListOf<ListItem>()
                                for(activitySnapshot in activities.children)
                                {
                                    val activityName = activitySnapshot.key
                                    activityName?.let{activityList.add(ListItem(it))}
                                }
                                for(hobbySnapshot in hobbies.children)
                                {
                                    val hobbyName = hobbySnapshot.key
                                    hobbyName?.let{hobbyList.add(ListItem(it))}
                                }
                                intent.putExtra("dateKey", dateKey)
                                intent.putExtra("holidayName", holidayName)
                                intent.putExtra("description", description)
                                intent.putExtra("country", countryName)
                                if(hobbyList.isNotEmpty() && activityList.isNotEmpty())
                                {
                                    intent.putExtra("hobbies", hobbyList.toTypedArray())
                                    intent.putExtra("activities", activityList.toTypedArray())
                                }
                                intent.putParcelableArrayListExtra("hobbies", ArrayList(hobbyList))
                                intent.putParcelableArrayListExtra("activities",ArrayList(activityList))
                                startActivity(intent)
                                (it.context as? AlertDialog)?.dismiss()
                            } else {
                                Toast.makeText(
                                    this,
                                    "You are not the author, you cannot modify!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        dialog.show()


        holidaysRef.child(dateKey).child(holidayName).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

                val description = dataSnapshot.child("description").getValue(String::class.java)

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

                holidayInfo.append("Description: $description\n")

                val countryString = country.joinToString(", ")
                holidayInfo.append("\nCountry: $countryString\n")

                holidayInfo.append("\nDate: $fullDate\n\n")

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
    private fun loadNickname()
    {
        val nickname = firebaseDatabase.getReference("UsersPersonalization").child(uid.toString()).child("nickname")
        nickname.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nick = snapshot.value.toString()
                val navigationView: NavigationView = findViewById(R.id.navigation_view)
                val headerView = navigationView.getHeaderView(0)
                profileImageView = headerView.findViewById(R.id.View_Image2)

                val username: TextView = headerView.findViewById(R.id.textView_username)
                username.text = nick
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error fetching nickname")
                Toast.makeText(applicationContext, "Error fetching nickname", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
