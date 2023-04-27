package com.example.howyoudoin.adapter

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.howyoudoin.R
import com.example.howyoudoin.localDatabase.ChatEntity

open class HomePageAdapter(private val listener: HomePageChatListener, private val permissionHandler: CustomPermissionInterface) : RecyclerView.Adapter<HomePageRecyclerViewHolder>(){
    private var chatsList: ArrayList<ChatEntity> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePageRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item_view, parent, false)
        val holder = HomePageRecyclerViewHolder(view)
        view.setOnClickListener{
            listener.onClick(holder)
        }
        return holder
    }

    override fun getItemCount(): Int {
        return chatsList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateChatsList(newChatsList : List<ChatEntity>){
        chatsList = newChatsList as ArrayList<ChatEntity>
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: HomePageRecyclerViewHolder, position: Int) {
        val model = chatsList[position]
        holder.lastTime.text = DateUtils.getRelativeTimeSpanString(model.lastTime,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS)
        if(model.name=="null"||model.name==model.number){
            permissionHandler.askForPermission(model.id)
        }
        holder.name.text = model.name
        holder.description.text = model.lastText
        holder.id = model.id
        Glide.with(holder.profilePhoto.context).load(R.drawable.human_photu).circleCrop().into(holder.profilePhoto)
        holder.number = model.number
    }

}

interface CustomPermissionInterface {
    fun askForPermission(chatId: String)
}

interface HomePageChatListener {
    fun onClick(holder: HomePageRecyclerViewHolder)
}


class HomePageRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    var description: TextView = itemView.findViewById(R.id.lastText)
    var name : TextView = itemView.findViewById(R.id.name)
    var profilePhoto : ImageView = itemView.findViewById(R.id.profilePhoto)
    var lastTime : TextView = itemView.findViewById(R.id.lastTime)
    lateinit var id: String
    lateinit var number: String
}