package com.example.howyoudoin.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.howyoudoin.Utility.MessagesTypeConverter

@Database(entities = [ChatEntity::class], version = 1, exportSchema = false)
@TypeConverters(MessagesTypeConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun getChatDao():ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}