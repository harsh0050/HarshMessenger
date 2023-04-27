package com.example.howyoudoin.Utility

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.howyoudoin.localDatabase.ChatDatabase
import com.example.howyoudoin.localDatabase.ChatEntity
import com.example.howyoudoin.model.Message
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

@OptIn(DelicateCoroutinesApi::class)
class ImageDownloadHandler(
    private val context: AppCompatActivity,
    private val processedInboxIds: HashSet<String>,
) {
    private var queue: BlockingQueue<QueryDocumentSnapshot> = LinkedBlockingQueue()
    private var chatDao = ChatDatabase.getDatabase(context).getChatDao()
    fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val document = queue.take()
                processedInboxIds.add(document.id)
                val intList = document.get("imageByteArray") as List<Int>
                val msg = Message().apply {
                    this.time = document.getLong("time")!!
                    this.displayTime = document.getString("displayTime")!!
                    this.mimeType = document.getString("mimeType")!!
                    this.sentBy = document.getString("sentBy")!!
                    this.chatId = document.getString("chatId")!!
                }

                val byteArray = intList.map { it.toByte() }.toByteArray()
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis().toString())
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val imageUri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                imageUri?.let {
                    val outputStream = context.contentResolver.openOutputStream(imageUri)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    contentValues.clear()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(imageUri, contentValues, null)
                    }
                    outputStream?.close()
                }
                msg.imageURI = imageUri.toString()
                chatDao.addChat(ChatEntity("null", msg.sentBy, msg.chatId))
                val chat = chatDao.getChat(msg.chatId)

                chat.messages.add(msg)
                chat.lastTime = msg.time
                chat.lastText = msg.message
                chatDao.updateChat(chat)
                document.reference.delete()
            }
        }

    }

    fun add(snapshot: QueryDocumentSnapshot) {
        queue.add(snapshot)
    }
}
