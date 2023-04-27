package com.example.howyoudoin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
//@TypeConverters(UriTypeConverter::class)
data class Message(@PrimaryKey var chatId: String) {
    @ColumnInfo
    var time: Long = 0L

    @ColumnInfo
    var displayTime: String = ""

    @ColumnInfo
    var sentBy: String = ""

    @ColumnInfo
    var message: String = ""

    @ColumnInfo
    var mimeType: String = "text/plain"

//    @TypeConverters(UriTypeConverter::class)
    @ColumnInfo
    var imageURI: String? = null

    constructor() : this("")//for firebase

    override fun toString(): String {
        return "["+time.toString()+","+displayTime+","+sentBy+","+message+","+mimeType+","+imageURI.toString()+"]"
    }
}
