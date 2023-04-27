package com.example.howyoudoin.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.howyoudoin.R
import com.example.howyoudoin.model.Contact

class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    var number : TextView = itemView.findViewById(R.id.contactNumber)
    var name : TextView = itemView.findViewById(R.id.contactName)
    var photo : ImageView = itemView.findViewById(R.id.contactProfilePhoto)
}

class SelectContactsAdapter(private val clickListener: CustomClickListener) : RecyclerView.Adapter<ContactViewHolder>() {
    private var contact = ArrayList<Contact>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item_view, parent, false)
        val holder = ContactViewHolder(view)
        view.setOnClickListener{
            clickListener.onClick(holder)
        }
        return holder
    }

    override fun getItemCount(): Int {
        return contact.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.name.text = contact[position].name
        holder.number.text = contact[position].number
        Glide.with(holder.photo.context).load(R.drawable.human_photu).circleCrop().into(holder.photo)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(nContact: ArrayList<Contact>){
        this.contact = nContact
        notifyDataSetChanged()
    }
}
interface CustomClickListener {
    fun onClick(holder: ContactViewHolder)
}