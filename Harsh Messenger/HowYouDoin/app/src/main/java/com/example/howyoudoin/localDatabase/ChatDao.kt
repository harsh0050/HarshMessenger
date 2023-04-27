package com.example.howyoudoin.localDatabase

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChatDao {
    @Insert(onConflict= OnConflictStrategy.IGNORE)
    suspend fun addChat(contact : ChatEntity)

    @Query("SELECT * FROM chat_table WHERE id = :id")
    suspend fun getChat(id : String): ChatEntity

    @Query("SELECT * FROM chat_table WHERE id = :id")
    fun getLiveChat(id : String): LiveData<ChatEntity>

    @Query("SELECT * FROM chat_table ORDER BY lastTime DESC")
    fun getAllChats():LiveData<List<ChatEntity>>

    @Update
    suspend fun updateChat(chat: ChatEntity)
}