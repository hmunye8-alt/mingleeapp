package com.timecompany.mingleeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private val messagesList = mutableListOf<Message>()

    private lateinit var editMessage: EditText
    private lateinit var sendButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var chatId: String? = null
    private var receiverPhone: String? = null
    private var receiverName: String? = null
    private var receiverImageUrl: String? = null
    private var currentPhone: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar: Toolbar = findViewById(R.id.chatToolbar)

        // Telefon numarasını al
        receiverPhone = normalizePhone(intent.getStringExtra("receiverPhone"))
        receiverImageUrl = intent.getStringExtra("receiverImageUrl")
        currentPhone = normalizePhone(currentUser?.phoneNumber)

        // Rehberden isim bul (local val kullanarak smart cast hatasını çözüyoruz)
        val phone = receiverPhone ?: ""
        receiverName = getContactName(phone)

        // Başlık: her zaman rehberdeki isim
        toolbar.title = receiverName ?: "Kişi adı bulunamadı"
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        recyclerView = findViewById(R.id.recyclerChat)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(messagesList)
        recyclerView.adapter = adapter

        editMessage = findViewById(R.id.editMessage)
        sendButton = findViewById(R.id.buttonSend)

        val participants = listOfNotNull(currentPhone, receiverPhone)
        chatId = participants.sorted().joinToString("-")

        loadMessages()

        sendButton.setOnClickListener {
            sendMessage()
        }

        onBackPressedDispatcher.addCallback(this) {
            val resultIntent = Intent()
            resultIntent.putExtra("lastChatPhone", receiverPhone)
            resultIntent.putExtra("lastChatName", receiverName)
            resultIntent.putExtra("lastChatImageUrl", receiverImageUrl)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    // Rehberden isim bulma fonksiyonu
    private fun getContactName(phoneNumber: String): String? {
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
        return null
    }

    private fun loadMessages() {
        if (chatId == null) return
        db.collection("chats").document(chatId!!)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                messagesList.clear()
                snapshot?.forEach { doc ->
                    val msg = doc.toObject(Message::class.java)
                    messagesList.add(msg)
                }
                adapter.notifyDataSetChanged()
                if (messagesList.isNotEmpty()) {
                    recyclerView.scrollToPosition(messagesList.size - 1)
                }
            }
    }

    private fun sendMessage() {
        val text = editMessage.text.toString()
        if (text.isNotEmpty() && chatId != null) {
            val msg = Message(
                sender = currentPhone,
                receiver = receiverPhone ?: "",
                message = text,
                timestamp = System.currentTimeMillis()
            )
            db.collection("chats").document(chatId!!)
                .collection("messages")
                .add(msg)

            val chatSummary = mapOf(
                "chatId" to chatId,
                "participants" to listOf(currentPhone, receiverPhone),
                "lastMessage" to text,
                "lastTimestamp" to System.currentTimeMillis(),
                "receiverName" to receiverName,
                "receiverImageUrl" to receiverImageUrl
            )
            db.collection("chats").document(chatId!!)
                .set(chatSummary, SetOptions.merge())

            editMessage.text.clear()
        }
    }

    private fun normalizePhone(phone: String?): String {
        if (phone.isNullOrEmpty()) return ""
        var normalized = phone.replace("\\s".toRegex(), "")
        if (normalized.startsWith("0")) {
            normalized = "+90" + normalized.substring(1)
        }
        return normalized
    }
}