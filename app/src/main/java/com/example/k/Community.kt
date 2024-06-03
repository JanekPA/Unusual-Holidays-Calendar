package com.example.k

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.k.databinding.CommunityBinding
import com.example.k.models.ListItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class Community : AppCompatActivity() {
    private lateinit var uid: String
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var binding: CommunityBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var profileImageView: ImageView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.let { user ->
            generateMessageButtons(user.uid)
        }
        setContentView(R.layout.community)
        binding = CommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        firebaseAuth = FirebaseAuth.getInstance()
        loadProfilePicture()
    }

    private fun loadNickname() {

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun setupViews() {


        binding.CalendarVCommunity.setOnClickListener {
            val intent = Intent(this, CalendarView::class.java)
            startActivity(intent)
        }
        binding.notificationCommunity?.setOnClickListener {
            val intent = Intent(this, Community::class.java)
            startActivity(intent)
        }
        binding.AddButtonCommunity.setOnClickListener {
            val intent = Intent(this, AddHolidayActivity::class.java)
            startActivity(intent)
        }
        binding.button4Community?.setOnClickListener {
            val intent = Intent(this, AddNotes::class.java)
            startActivity(intent)
        }
        binding.homeCommunity?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        drawerLayout = binding.myDrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
                Glide.with(this@Community)
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
    private fun generateMessageButtons(uid: String) {
        val messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(uid)

        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val messagesLayout: LinearLayout? = findViewById(R.id.messageslayout)

                if (messagesLayout == null) {
                    Log.e("Community", "messagesLayout is null")
                    return
                }

                messagesLayout.removeAllViews()

                for (messageSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(String::class.java)

                    if (message != null) {

                        val messageButtonView = layoutInflater.inflate(R.layout.messagebutton, null) as Button
                        val messageButton = messageButtonView.findViewById<Button>(R.id.MessageButton)


                        messageButton.text = message


                        messageButton.setOnClickListener {
                            Toast.makeText(this@Community, message, Toast.LENGTH_SHORT).show()
                        }


                        messagesLayout.addView(messageButtonView)
                    }
                }

                if (messagesLayout.childCount == 0) {
                    // If no messages found, display a message
                    val noMessagesButton = Button(this@Community)
                    noMessagesButton.text = "No messages found"
                    messagesLayout.addView(noMessagesButton)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Community", "Error fetching messages: ${databaseError.message}")
                Toast.makeText(this@Community, "Error fetching messages.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}