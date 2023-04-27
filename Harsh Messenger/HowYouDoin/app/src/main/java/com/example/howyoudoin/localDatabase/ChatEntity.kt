package com.example.howyoudoin.localDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.howyoudoin.Utility.MessagesTypeConverter
import com.example.howyoudoin.model.Message

@Entity(tableName = "chat_table")
data class ChatEntity(var name: String, val number: String, @PrimaryKey val id:String) {
    var lastTime: Long = 0L
    var lastText: String = ""

    @TypeConverters(MessagesTypeConverter::class)
    var messages : ArrayList<Message> = ArrayList()

    override fun toString(): String {
        return "{$name,$number,$id,$lastTime,$lastText}::\n$messages"
    }
}