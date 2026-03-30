package com.timecompany.mingleeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatListAdapter(
    private val chats: List<Chat>,
    private val onClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textName)
        val lastMessageText: TextView = itemView.findViewById(R.id.textLastMessage)
        val profileImage: ImageView = itemView.findViewById(R.id.imageProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        // Null güvenliği: receiverName boşsa fallback
        holder.nameText.text = chat.receiverName?.takeIf { it.isNotEmpty() } ?: "Bilinmeyen Kişi"

        // Null güvenliği: lastMessage boşsa fallback
        holder.lastMessageText.text = chat.lastMessage?.takeIf { it.isNotEmpty() } ?: "Henüz mesaj yok"

        // Profil fotoğrafı varsa Glide ile yükle, yoksa default icon
        if (!chat.receiverImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(chat.receiverImageUrl)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_person) // fallback icon
        }

        // Tıklama olayı
        holder.itemView.setOnClickListener { onClick(chat) }
    }

    override fun getItemCount() = chats.size
}