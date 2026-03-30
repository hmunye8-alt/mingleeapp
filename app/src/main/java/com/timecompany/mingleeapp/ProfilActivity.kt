package com.timecompany.mingleeapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val bio: String = "",
    val imageUrl: String = ""
)

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImage: ImageView
    private lateinit var editName: EditText
    private lateinit var editBio: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonSelectImage: Button

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImage = findViewById(R.id.profileImage)
        editName = findViewById(R.id.editName)
        editBio = findViewById(R.id.editBio)
        buttonSave = findViewById(R.id.buttonSave)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)

        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        buttonSave.setOnClickListener {
            saveProfile()
        }

        loadUserProfile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            profileImage.setImageURI(imageUri)
        }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val name = editName.text.toString()
        val bio = editBio.text.toString()
        val phone = auth.currentUser?.phoneNumber ?: ""

        if (imageUri != null) {
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(imageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        val profile = UserProfile(uid, name, phone, bio, uri.toString())
                        saveToFirestore(profile)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Fotoğraf yüklenemedi!", Toast.LENGTH_SHORT).show()
                }
        } else {
            val profile = UserProfile(uid, name, phone, bio, "")
            saveToFirestore(profile)
        }
    }

    private fun saveToFirestore(profile: UserProfile) {
        firestore.collection("users").document(profile.uid).set(profile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil kaydedildi!", Toast.LENGTH_SHORT).show()
                // Ana sayfaya yönlendir
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(UserProfile::class.java)
                    user?.let {
                        editName.setText(it.name)
                        editBio.setText(it.bio)
                        // imageUrl varsa Glide/Picasso ile profileImage’e yüklenebilir
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Profil yüklenemedi!", Toast.LENGTH_SHORT).show()
            }
    }
}