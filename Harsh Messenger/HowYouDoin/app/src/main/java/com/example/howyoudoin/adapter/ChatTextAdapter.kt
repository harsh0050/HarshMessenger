package com.example.howyoudoin.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.howyoudoin.R
import com.example.howyoudoin.model.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


//class SentTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
//    var text : TextView = itemView.findViewById(R.id.sentText)
//    val time : TextView = itemView.findViewById(R.id.sentTime)
//}
//class ReceivedTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
//    var text : TextView = itemView.findViewById(R.id.receivedText)
//    val time : TextView = itemView.findViewById(R.id.receivedTime)
//}
//class SentImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
//    var image : ImageView = itemView.findViewById(R.id.sentImg)
//    val time : TextView = itemView.findViewById(R.id.imgSentTime)
//}
//class ReceivedImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
//    var image : ImageView = itemView.findViewById(R.id.receivedImg)
//    val time : TextView = itemView.findViewById(R.id.imgReceivedTime)
//}
class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
class ChatTextAdapter :
    RecyclerView.Adapter<MessageViewHolder>() {
    private var messages: ArrayList<Message> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        when (viewType) {

            11 -> {//sent text
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.text_sent_item_view, parent, false)
                return MessageViewHolder(view)
            }
            21 -> {//received text
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.text_received_item_view, parent, false)
                return MessageViewHolder(view)
            }
            12 -> {//sent img
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.image_sent_item_view, parent, false)
                return MessageViewHolder(view)
            }
            else -> {//received img
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.image_received_item_view, parent, false)
                return MessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val viewType = holder.itemViewType
        val model = messages[position]
        when (viewType) {
            11 -> {//sent text
                holder.itemView.findViewById<TextView>(R.id.sentText).text = model.message
                holder.itemView.findViewById<TextView>(R.id.sentTime).text = model.displayTime
            }
            21 -> {//received text
                holder.itemView.findViewById<TextView>(R.id.receivedText).text = model.message
                holder.itemView.findViewById<TextView>(R.id.receivedTime).text = model.displayTime
            }
            12 -> {//sent image
                val sentImageView = holder.itemView.findViewById<ImageView>(R.id.sentImg)
                Glide.with(sentImageView.context).load(Uri.parse(model.imageURI)).into(sentImageView)
                holder.itemView.findViewById<TextView>(R.id.imgSentTime).text = model.displayTime
            }
            else -> {//received image
                val receivedImageView= holder.itemView.findViewById<ImageView>(R.id.receivedImg)
                Glide.with(receivedImageView.context).load(Uri.parse(model.imageURI)).into(receivedImageView)
                holder.itemView.findViewById<TextView>(R.id.imgReceivedTime).text = model.displayTime
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val sentBy = messages[position].sentBy
        if(sentBy==Firebase.auth.currentUser!!.phoneNumber){//1
            return when(messages[position].mimeType){
                "text/plain"->
                    11
                "image/jpeg"->
                    12
                else->
                    -1
            }
        }else{//2
            return when(messages[position].mimeType){
                "text/plain"->
                    21
                "image/jpeg"->
                    22
                else->
                    -1
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun updateMessages(newList: ArrayList<Message>){
        messages = newList
        notifyDataSetChanged()
    }

}