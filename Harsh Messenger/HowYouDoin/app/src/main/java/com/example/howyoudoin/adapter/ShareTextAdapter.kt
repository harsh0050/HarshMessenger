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
import com.example.howyoudoin.localDatabase.ChatEntity


class ShareTextAdapter(
    private val listener: ShareTextListener
) : RecyclerView.Adapter<ShareTextViewHolder>() {
    private var allChats: List<ChatEntity> = ArrayList()
    override fun onBindViewHolder(
        holder: ShareTextViewHolder,
        position: Int,
    ) {
        val model = allChats[position]
        holder.name.text = model.name
        holder.id = model.id
        Glide.with(holder.profilePhoto.context).load(R.drawable.human_photu).circleCrop().into(holder.profilePhoto)
        holder.number = model.number
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareTextViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.share_to_item_view, parent, false)
        val holder = ShareTextViewHolder(view).apply {
            this.selectedPhoto.visibility = View.GONE
        }
        view.setOnClickListener{
            listener.onClick(holder)
        }
        return holder
    }

    override fun getItemCount(): Int {
        return allChats.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<ChatEntity>){
        allChats = newList
        notifyDataSetChanged()
    }
}
interface ShareTextListener{
    fun onClick(holder: ShareTextViewHolder)
}
class ShareTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var description: TextView = itemView.findViewById(R.id.shareToDescription)
    var name : TextView = itemView.findViewById(R.id.shareToName)
    var profilePhoto : ImageView = itemView.findViewById(R.id.shareToProfilePhoto)
    var selectedPhoto : ImageView = itemView.findViewById(R.id.selectedImage)
    lateinit var id: String
    lateinit var number: String
    var isSelected = false
}

