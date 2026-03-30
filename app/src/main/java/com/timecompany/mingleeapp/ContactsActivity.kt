package com.timecompany.mingleeapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ContactsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private val contactsList = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        recyclerView = findViewById(R.id.recyclerContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ContactsAdapter(contactsList) { phone: String ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverPhone", phone)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 100)
        } else {
            loadContacts()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        }
    }

    private fun normalizePhoneNumber(phone: String): String {
        return phone.replace("\\s".toRegex(), "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .let {
                if (it.startsWith("0")) "+90" + it.substring(1)
                else it
            }
    }

    private fun loadContacts() {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        contactsList.clear()
        while (cursor?.moveToNext() == true) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contactsList.add(Pair(name, phone))
        }
        cursor?.close()

        matchContactsWithFirebase()
    }

    private fun matchContactsWithFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get().addOnSuccessListener { result ->
            val firebaseUsers = result.documents.mapNotNull { it.getString("phone") }
            val matchedContacts = contactsList.filter { contact ->
                val normalized = normalizePhoneNumber(contact.second)
                firebaseUsers.contains(normalized)
            }
            contactsList.clear()
            contactsList.addAll(matchedContacts)
            adapter.notifyDataSetChanged()
        }
    }
}