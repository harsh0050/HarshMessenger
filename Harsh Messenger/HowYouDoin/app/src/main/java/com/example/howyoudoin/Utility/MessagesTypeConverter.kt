package com.example.howyoudoin.Utility

import androidx.room.TypeConverter
import com.example.howyoudoin.model.Message
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MessagesTypeConverter {
    @TypeConverter
    fun fromMessage(msgList : ArrayList<Message>): String? {
        return Gson().toJson(msgList)
    }

    @TypeConverter
    fun toMessage(msg: String?): ArrayList<Message>? {
        val type =  object : TypeToken<ArrayList<Message>>(){}.type
        return Gson().fromJson(msg, type)
    }
}