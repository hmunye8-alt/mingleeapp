package com.timecompany.mingleeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(
    private val contacts: List<Pair<String, String>>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textName)
        val phoneText: TextView = itemView.findViewById(R.id.textPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val (name, phone) = contacts[position]
        holder.nameText.text = name
        holder.phoneText.text = phone

        holder.itemView.setOnClickListener {
            onItemClick(phone) // kişiye tıklanınca ChatActivity’ye geçiş
        }
    }

    override fun getItemCount(): Int = contacts.size
}