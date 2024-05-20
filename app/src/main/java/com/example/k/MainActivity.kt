package com.example.k

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.k.databinding.ActivityMainBinding
import com.example.k.models.ListItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var profileImageView: ImageView
    private val REQUEST_EXTERNAL_STORAGE = 1
    private lateinit var uid: String
    private lateinit var calendarTextView: TextView
    private lateinit var notesTextView: Button
    private lateinit var yourNotesTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        calendarTextView = binding.idDate!!
        notesTextView = binding.editNotes!!
        yourNotesTextView = binding.yourNotes!!


        val yourNotes = "Your notes for today:"
        yourNotesTextView.text = yourNotes

        firebaseAuth = FirebaseAuth.getInstance()
        setupViews()
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


        if (!checkExternalStoragePermission()) {
            requestExternalStoragePermission()
        }
        loadProfilePicture()

        displayCurrentDate()

        loadNotes()

        retrievingDataToEditNotes()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun displayCurrentDate() {
        val currentDate = Calendar.getInstance().time
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val day = dayFormat.format(currentDate)
        val month = monthFormat.format(currentDate).toUpperCase(Locale.getDefault())
        val year = yearFormat.format(currentDate)

        findViewById<TextView>(R.id.yearTextView).text = year

        val formattedDate = "$day\n${month.capitalize(Locale.getDefault())}"

        calendarTextView.text = formattedDate
    }

    private fun setupViews() {
        binding.AccountButton?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.CalendarV?.setOnClickListener {
            val intent = Intent(this, CalendarView::class.java)
            startActivity(intent)
        }
        binding.AddButton?.setOnClickListener {
            val intent = Intent(this, AddHolidayActivity::class.java)
            startActivity(intent)
        }
        binding.communityMain?.setOnClickListener {
            val intent = Intent(this, Community::class.java)
            startActivity(intent)
        }
        binding.button4?.setOnClickListener {
            val intent = Intent(this, AddNotes::class.java)
            startActivity(intent)
        }
        drawerLayout = binding.myDrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    override fun onResume() {
        super.onResume()
        loadProfilePicture()
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
                Glide.with(this@MainActivity)
                    .load(uri)
                    .into(profileImageView)
            }.addOnFailureListener { exception ->
                // errors
                Log.e("MainActivity", "Error loading profile picture from Firebase Storage: $exception")
            }
        }
    }

    private fun checkExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Unable to access external storage.", Toast.LENGTH_SHORT).show()
            }
        }
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
                    val hobbies = persSnapshot.child("Hobbies")
                    val activities = persSnapshot.child("Activities")
                    ///AKTYWNOSC + HOBBY - POBRANIE
                    val hobbyList = mutableListOf<ListItem>()
                    for(hobbySnapshot in hobbies.children)
                    {
                        val hobbyName = hobbySnapshot.key
                        hobbyName?.let {hobbyList.add(ListItem(it))}
                    }

                    ///AKTYWNOSC + HOBBY - POBRANIE
                    val activityList = mutableListOf<ListItem>()
                    for(activitySnapshot in activities.children)
                    {
                        val activityName = activitySnapshot.key
                        activityName?.let {activityList.add(ListItem(it))}
                    }
                    val intent = Intent(this, PrefChanging::class.java)
                    intent.putExtra("countryName", countryName)
                    if(hobbyList.isNotEmpty() && activityList.isNotEmpty()) {
                        intent.putExtra("hobbies", hobbyList.toTypedArray())
                        for(hobby in hobbyList)
                        {
                            Log.e("HOBBY",hobby.name) ///working properly
                        }
                        intent.putExtra("activities", activityList.toTypedArray())
                        for(activity in activityList)
                        {
                            Log.e("ACTIVITY", activity.name) ///working properly
                        }
                    }
                    intent.putParcelableArrayListExtra("hobbies", ArrayList(hobbyList))
                    intent.putParcelableArrayListExtra("activities",ArrayList(activityList))

                    startActivity(intent)
                }
            }

        }
    }

    private fun retrievingDataToEditNotes()
    {
        val editButton = findViewById<Button>(R.id.editNotes)

        val firebaseAuth = FirebaseAuth.getInstance()

        val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        editButton.setOnClickListener {

            val firebaseUser = firebaseAuth.currentUser
            firebaseUser?.let { user ->
                val uid = user.uid

                val firebaseRef = FirebaseDatabase.getInstance().getReference("Notes")

                firebaseRef.child(uid).child(todayDate).get()
                    .addOnSuccessListener { notesSnapshot ->
                        if (notesSnapshot.exists()) {
                            val text = notesSnapshot.child("text").getValue(String::class.java)
                            val intent = Intent(this, EditNotes::class.java)
                            intent.putExtra("dateKey", todayDate)
                            intent.putExtra("text", text)
                            startActivity(intent)
                        }
                        else
                        {
                            val intent = Intent(this, EditNotes::class.java)
                            intent.putExtra("dateKey", todayDate)
                            startActivity(intent)
                        }
                    }
            }
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



    private fun loadNotes() {

        val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser

        firebaseUser?.let { user ->
            val uid = user.uid
            val holidaysRef = firebaseDatabase.getReference("Notes")

            holidaysRef.child(uid).child(todayDate).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val text = dataSnapshot.child("text").getValue(String::class.java)
                    notesTextView.text = text.toString()

                    }
                else
                    {
                        notesTextView.text = "You have no notes for this day"
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Error fetching holiday details.", Toast.LENGTH_SHORT).show()
            }
        }
    }



}