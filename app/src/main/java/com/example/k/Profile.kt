package com.example.k

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {
    private lateinit var storageReference: StorageReference
    private lateinit var imageViewProfile: ImageView
    private lateinit var uid: String
    private val REQUEST_EXTERNAL_STORAGE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        storageReference = FirebaseStorage.getInstance().reference
        imageViewProfile = findViewById(R.id.View_Image)
        val uploadButton: Button = findViewById(R.id.upload_image)

        uploadButton.setOnClickListener {
            openImageChooser()
        }
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        loadImageFromFirebase()
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

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
                Log.d(TAG, "Image uploaded successfully")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error uploading image: $exception")
            }
    }

    private fun loadImageFromFirebase() {
        val profilePicturesRef = storageReference.child("ProfilePictures/$uid/")
        val filename = "profile.jpg"
        val imageRef = profilePicturesRef.child(filename)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(imageViewProfile)
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error loading image from Firebase Storage: $exception")
        }
    }
    companion object {
        private const val TAG = "ProfileActivity"
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
}