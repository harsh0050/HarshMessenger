package com.example.howyoudoin.Utility

import android.net.Uri
import androidx.room.TypeConverter

class UriTypeConverter {
    @TypeConverter
    fun fromUri(uri: Uri?): String {
        return uri.toString()
    }
    @TypeConverter
    fun toUri(uri: String): Uri? {
        return Uri.parse(uri)
    }

}