package com.timecompany.mingleeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private val chatList = mutableListOf<Chat>()

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Minglee"

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerChats)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatListAdapter(chatList) { chat ->
            openChat(chat)
        }
        recyclerView.adapter = adapter

        // BottomNavigation listener
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> true
                R.id.action_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // FAB listener
        val fab = findViewById<FloatingActionButton>(R.id.fabContacts)
        fab.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        // Runtime izin kontrolü
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 100)
        } else {
            loadChats()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadChats()
        }
    }

    private fun loadChats() {
        db.collection("chats")
            .addSnapshotListener { snapshot, _ ->
                chatList.clear()
                snapshot?.forEach { doc ->
                    val chat = doc.toObject(Chat::class.java)
                    val otherPhone = chat.participants.find { it != currentUser?.phoneNumber }
                    val contactName = otherPhone?.let { getContactName(it) }
                    val updatedChat = chat.copy(
                        receiverName = contactName ?: otherPhone ?: "",
                        receiverImageUrl = chat.receiverImageUrl ?: ""
                    )
                    chatList.add(updatedChat)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun openChat(chat: Chat) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("receiverPhone", chat.participants.find { it != currentUser?.phoneNumber })
        intent.putExtra("receiverName", chat.receiverName)
        intent.putExtra("receiverImageUrl", chat.receiverImageUrl)
        startActivity(intent)
    }

    private fun getContactName(phoneNumber: String): String? {
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            return phoneNumber // izin yoksa numarayı göster
        }

        val uri = Uri.withAppendedPath(
            android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME)

        contentResolver.query(uri, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return phoneNumber
    }
}