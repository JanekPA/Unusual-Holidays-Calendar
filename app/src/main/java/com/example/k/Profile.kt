package com.example.k

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.k.databinding.CommunityBinding
import com.example.k.databinding.ProfileBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser

class Profile : AppCompatActivity() {

    private lateinit var storageReference: StorageReference
    private lateinit var imageViewProfile: ImageView
    private lateinit var uid: String
    private val REQUEST_EXTERNAL_STORAGE = 1
    private lateinit var infoTextView: TextView
    private lateinit var binding: ProfileBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var profileImageView: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageReference = FirebaseStorage.getInstance().reference
        imageViewProfile = findViewById(R.id.View_Image)
        val uploadButton: Button = findViewById(R.id.upload_image)
        infoTextView = findViewById(R.id.info_text_view)
        setupViews()
        uploadButton.setOnClickListener {
            openImageChooser()
        }
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        loadImageFromFirebase()

        val b1 = findViewById<ImageButton>(R.id.B1)
        val b2 = findViewById<ImageButton>(R.id.B2)
        val b3 = findViewById<ImageButton>(R.id.B3)

        b1.setOnClickListener {
            infoTextView.text = "Information for B1"
        }

        b2.setOnClickListener {
            infoTextView.text = "Information for B2"
        }

        b3.setOnClickListener {
            infoTextView.text = "Information for B3"
        }
        infoTextView.text = "Information for B1"

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
    }

    private fun openImageChooser() {
        if (!checkExternalStoragePermission()) {
            requestExternalStoragePermission()
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let {
                    uploadImageToFirebase(it)
                }
            }
        }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val profilePicturesRef = storageReference.child("ProfilePictures/$uid/")
        val filename = "profile.jpg"
        val imageRef = profilePicturesRef.child(filename)
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                loadImageFromFirebase()
                println("Image uploaded successfully")
            }
            .addOnFailureListener { exception ->
                println("Error uploading image: $exception")
            }
    }

    private fun loadImageFromFirebase() {
        val profilePicturesRef = storageReference.child("ProfilePictures/$uid/")
        val filename = "profile.jpg"
        val imageRef = profilePicturesRef.child(filename)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(imageViewProfile)
        }.addOnFailureListener { exception ->
            println("Error loading image from Firebase Storage: $exception")
        }
    }

    private fun checkExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(
                    this,
                    "Permission denied. Unable to access external storage.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupViews() {
        binding.AccountButton?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.CalendarVProfile.setOnClickListener {
            val intent = Intent(this, CalendarView::class.java)
            startActivity(intent)
        }
        binding.AddButtonProfile?.setOnClickListener {
            val intent = Intent(this, AddHolidayActivity::class.java)
            startActivity(intent)
        }
        binding.communityProfile?.setOnClickListener {
            val intent = Intent(this, Community::class.java)
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
                Glide.with(this@Profile)
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
}