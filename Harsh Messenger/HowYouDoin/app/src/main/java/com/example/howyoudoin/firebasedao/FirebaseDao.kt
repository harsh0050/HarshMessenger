package com.example.howyoudoin.firebasedao

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import com.example.howyoudoin.localDatabase.ChatDatabase
import com.example.howyoudoin.localDatabase.ChatEntity
import com.example.howyoudoin.model.Contact
import com.example.howyoudoin.model.Message
import com.example.howyoudoin.model.TempMessageForDatabase
import com.example.howyoudoin.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(DelicateCoroutinesApi::class)
class FirebaseDao(private val context: Context) {
    private val db = Firebase.firestore.collection("users")
    private val authNum = Firebase.auth.currentUser!!.phoneNumber!!
    private val chatDao = ChatDatabase.getDatabase(context).getChatDao()

    fun addUser(userNumber: String): Task<Void>? {
        val user = getUserByNumber(userNumber)
        if (user == null) {
            val newUser = User(userNumber, "default")
            return db.document(userNumber).set(newUser)
        }
        return null
    }

    private fun getUserByNumber(number: String): User? {
        var user: User? = null
        db.document(number).get().addOnSuccessListener {
            val result = it
            if (result.exists()) {
                val num = result.get("number").toString()
                val displayName = result.get("displayName").toString()
                user = User(num, displayName)
            }
        }
        return user
    }

    fun addNewChat(fNumber: String, fName: String, time: Long) {
        val friendNumber = formatNumber(fNumber)

        Firebase.firestore
            .collection("ChatDatabase")
            .add(hashMapOf("lastTime" to time, "lastText" to "")).addOnSuccessListener { docRef ->
                GlobalScope.launch {

                    //add to local
                    chatDao.addChat(ChatEntity(fName, friendNumber, docRef.id).apply {
                        this.lastTime = time
                    })


                    //self
                    val chat = db.document(authNum).collection("chats").document(friendNumber)
                    var contact = Contact(fName, friendNumber, docRef.id).apply {
                        lastText = ""
                        lastTime = time
                    }
                    chat.set(contact).addOnFailureListener {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }

                    //add user
                    val task = addUser(friendNumber)
                    task?.addOnSuccessListener {
                        //friend
                        val fChat = db.document(friendNumber).collection("chats").document(authNum)
                        contact = Contact(
                            Firebase.auth.currentUser!!.displayName.toString(),
                            authNum,
                            docRef.id
                        ).apply {
                            lastText = ""
                            lastTime = time
                        }
                        fChat.set(contact)
                    }


                }
            }


    }

    fun addText(
        message: String,
        time: Long,
        displayTime: String,
        sendToNumber: String,
        id: String,
    ) {
        val msg = Message(id).apply {
            this.message = message
            this.sentBy = authNum
            this.displayTime = displayTime
            this.time = time
            this.chatId = id
        }
        Firebase.firestore.collection("ChatDatabase/$id/textCollection").add(msg)
        Firebase.firestore.collection("ChatDatabase").document(id)
            .set(hashMapOf("lastTime" to time, "lastText" to message))
        Firebase.firestore.collection("users/$sendToNumber/inbox").add(msg)
        GlobalScope.launch {
            val newChat = chatDao.getChat(id).apply {
                messages.add(msg)
                this.lastText = msg.message
                this.lastTime = msg.time
            }
            chatDao.updateChat(newChat)
        }
    }

    fun addImage(time: Long, displayTime: String, sendToNumber: String, id: String, imageURI: Uri) {
        val msg = Message(id).apply {
            this.time = time
            this.chatId = id
            this.displayTime = displayTime
            this.sentBy = authNum
            this.mimeType = "image/jpeg"
            this.imageURI = imageURI.toString()
        }

        Firebase.firestore.collection("ChatDatabase").document(id)
            .set(hashMapOf("lastTime" to time, "lastText" to "photo"))
        GlobalScope.launch {
            val tempMsg = TempMessageForDatabase.convert(msg)
            val outputStream = ByteArrayOutputStream()
            val inputStream = context.contentResolver.openInputStream(imageURI)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)


            tempMsg.imageByteArray = outputStream.toByteArray().map{
                it.toInt()
            }.toList()

            Firebase.firestore.collection("users/$sendToNumber/inbox").add(tempMsg)
            val newChat = chatDao.getChat(id).apply {
                messages.add(msg)
                this.lastText = "photo"
                this.lastTime = time
            }
            chatDao.updateChat(newChat)
        }
    }

    fun updateLastTime(id: String, fNumber: String, endTime: Long, endText: String) {
        val chat = Firebase.firestore.collection("users/$authNum/chats").document(fNumber)
        chat.update("lastTime", endTime)
        chat.update("lastText", endText)
        val fChat = Firebase.firestore.collection("users/$fNumber/chats").document(authNum)
        fChat.update("lastTime", endTime)
        fChat.update("lastText", endText)
    }

    companion object {
        fun formatNumber(number: String): String {
            val strBuilder = StringBuilder()
            val temp = number.trim()
            if (temp[0] != '+') {
                strBuilder.append("+91")
            }
            for (i in temp.indices) {
                if (temp[i] == '+' || (temp[i] in '0'..'9')) {
                    strBuilder.append(temp[i])
                }
            }
            return strBuilder.toString()
        }
    }

}
