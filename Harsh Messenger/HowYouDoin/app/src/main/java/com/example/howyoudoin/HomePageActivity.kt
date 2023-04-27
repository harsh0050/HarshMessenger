package com.example.howyoudoin

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.howyoudoin.Utility.ImageDownloadHandler
import com.example.howyoudoin.adapter.CustomPermissionInterface
import com.example.howyoudoin.adapter.HomePageAdapter
import com.example.howyoudoin.adapter.HomePageChatListener
import com.example.howyoudoin.adapter.HomePageRecyclerViewHolder
import com.example.howyoudoin.databinding.ActivityHomePageBinding
import com.example.howyoudoin.firebasedao.FirebaseDao
import com.example.howyoudoin.localDatabase.ChatDao
import com.example.howyoudoin.localDatabase.ChatDatabase
import com.example.howyoudoin.localDatabase.ChatEntity
import com.example.howyoudoin.model.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class HomePageActivity : AppCompatActivity(), HomePageChatListener, CustomPermissionInterface {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var toolbar: Toolbar
    private lateinit var chatId: String
    private lateinit var firebaseDao: FirebaseDao
    private lateinit var chatDao: ChatDao
    private var isStoragePermitted = false

    private val storageAccessPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                isStoragePermitted = true
                load(chatDao)
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Please accept the request to send/receive images",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    private val contactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                findAndUpdateContact(chatId)
            } else {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Can't access Contacts",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDao = FirebaseDao(this)
        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val homePageAdapter = HomePageAdapter(this, this)
        binding.recyclerView.adapter = homePageAdapter

        chatDao = ChatDatabase.getDatabase(this).getChatDao()
        load(chatDao)

        chatDao.getAllChats().observe(this) {
            homePageAdapter.updateChatsList(it)
        }

        binding.addChatButton.setOnClickListener {
            val addNewChatIntent = Intent(this, SelectContactActivity::class.java)
            startActivity(addNewChatIntent)
        }

    }


    private val processedInboxIds = HashSet<String>()
    private fun load(chatDao: ChatDao) {
        val imageDownloadHandler = ImageDownloadHandler(this, processedInboxIds)
        imageDownloadHandler.start()

        val query = Firebase.firestore
            .collection("users/$authNumber/inbox")
            .orderBy("time", Query.Direction.ASCENDING)

        query.addSnapshotListener { value, _ ->
            GlobalScope.launch(Dispatchers.IO) {
                for (docChanges in value!!.documentChanges) {
                    var redFlag = false
                    when (docChanges.type) {
                        DocumentChange.Type.ADDED -> {
                            val document = docChanges.document
                            if (processedInboxIds.contains(document.id)) {
                                break
                            }

                            if (document.getString("mimeType")!! == "text/plain") {
                                val msg = Message().apply {
                                    this.time = document.getLong("time")!!
                                    this.displayTime = document.getString("displayTime")!!
                                    this.mimeType = document.getString("mimeType")!!
                                    this.sentBy = document.getString("sentBy")!!
                                    this.chatId = document.getString("chatId")!!
                                }
                                msg.message = document.getString("message")!!
                                processedInboxIds.add(document.id)
                                chatDao.addChat(ChatEntity("null", msg.sentBy, msg.chatId))
                                chatDao.updateChat(chatDao.getChat(msg.chatId).apply {
                                    lastText = msg.message
                                    lastTime = msg.time
                                    messages.add(msg)
                                })

                                document.reference.delete()

                            } else if (document.getString("mimeType")!! == "image/jpeg") {
                                if (isStoragePermitted) {
                                    imageDownloadHandler.add(document)
                                } else {
                                    storageAccessPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    redFlag = true
                                }


                            }

                        }
                        else -> {
                            //do nothing
                        }
                    }
                    if (redFlag) {
                        break
                    }
                }
            }
        }
    }


    @SuppressLint("Range")
    fun findAndUpdateContact(chatId: String) {

        GlobalScope.launch(Dispatchers.IO) {
            val chat = chatDao.getChat(chatId)
            val num = chat.number
            var flag = false
            val cursor: Cursor? =
                contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val hasNumber =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            .toInt()
                    if (hasNumber > 0) {
                        val id =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        val phoneCursor: Cursor? = contentResolver
                            .query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(id),
                                null
                            )
                        if (phoneCursor != null) {
                            if (phoneCursor.moveToNext()) {
                                val number = phoneCursor.getString(
                                    phoneCursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER
                                    )
                                )
                                val formatted = FirebaseDao.formatNumber(number)
                                if (formatted == num) {

                                    val name = phoneCursor.getString(
                                        phoneCursor.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                                        )
                                    )
                                    chatDao.updateChat(chat.apply {
                                        this.name = name
                                    })
                                    phoneCursor.close()
                                    flag = true
                                    break
                                }
                            }
                            phoneCursor.close()
                        }
                    }
                }
                cursor.close()
                if (!flag) {
                    chatDao.updateChat(chat.apply {
                        this.name = this.number
                    })
                }
            }
        }
    }

    override fun onClick(holder: HomePageRecyclerViewHolder) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("name", holder.name.text)
        intent.putExtra("id", holder.id)
        intent.putExtra("number", FirebaseDao.formatNumber(holder.number))
        startActivity(intent)
    }

    companion object {
        val authNumber = Firebase.auth.currentUser!!.phoneNumber!!
    }

    override fun askForPermission(chatId: String) {
        this.chatId = chatId
        contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
    }


}