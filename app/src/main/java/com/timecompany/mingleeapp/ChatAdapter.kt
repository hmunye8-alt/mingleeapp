package com.timecompany.mingleeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val currentUserPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.messageContainer)
        val textMessage: TextView = itemView.findViewById(R.id.textMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]
        holder.textMessage.text = msg.message

        if (msg.sender == currentUserPhone) {
            // Bizim mesajımız: sağda, yeşil baloncuk
            holder.textMessage.setBackgroundResource(R.drawable.bg_message_me)
            val params = holder.container.layoutParams as ViewGroup.MarginLayoutParams
            params.marginStart = 100
            params.marginEnd = 0
            holder.container.layoutParams = params
            holder.container.gravity = android.view.Gravity.END
        } else {
            // Karşı tarafın mesajı: solda, gri baloncuk
            holder.textMessage.setBackgroundResource(R.drawable.bg_message_other)
            val params = holder.container.layoutParams as ViewGroup.MarginLayoutParams
            params.marginStart = 0
            params.marginEnd = 100
            holder.container.layoutParams = params
            holder.container.gravity = android.view.Gravity.START
        }
    }

    override fun getItemCount(): Int = messages.size
}