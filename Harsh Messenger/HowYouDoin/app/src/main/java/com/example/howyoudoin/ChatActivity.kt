package com.example.howyoudoin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.howyoudoin.adapter.ChatTextAdapter
import com.example.howyoudoin.databinding.ActivityChatBinding
import com.example.howyoudoin.firebasedao.FirebaseDao
import com.example.howyoudoin.localDatabase.ChatDatabase
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(DelicateCoroutinesApi::class)
class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var toolbar: Toolbar
    private lateinit var context: AppCompatActivity
    private val firebaseDao = FirebaseDao(this)
    private lateinit var id: String
    private val adapter = ChatTextAdapter()
    private var time: Long? = null
    private var lastMessage: String? = null

    private val activityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val time = System.currentTimeMillis()
                val displayTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    DateTimeFormatter.ofPattern("hh:mm a").format(LocalDateTime.now())
                } else {
                    "Buy a new mobile"
                }
                firebaseDao
                    .addImage(
                        time,
                        displayTime,
                        intent.getStringExtra("number")!!,
                        id,
                        it.data?.data!!//image uri
                    )
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        id = intent.getStringExtra("id").toString()
        context = this

        setContentView(binding.root)
        toolbar = binding.chatToolBar
        val chatDao = ChatDatabase.getDatabase(this).getChatDao()

        binding.contactName.text = intent.getStringExtra("name")
        Glide.with(this).load(R.drawable.human_photu).circleCrop().into(binding.contactProfilePhoto)
        setSupportActionBar(toolbar)

        binding.textRecyclerView
            .layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)

        GlobalScope.launch(Dispatchers.Main) {
            Transformations.map(chatDao.getLiveChat(id)) {
                it.messages
            }.observe(context) {
                it.reverse()
                adapter.updateMessages(it)
            }

        }


        binding.chatProgressBar.visibility = View.GONE
        binding.textRecyclerView.adapter = adapter

        intent.getStringExtra("text")?.let {
            binding.messageInput.setText(it)
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            if (message.isNotBlank()) {
                time = System.currentTimeMillis()
                lastMessage = message
                firebaseDao
                    .addText(
                        message, time!!,
                        DateTimeFormatter.ofPattern("hh:mm a").format(LocalDateTime.now()),
                        intent.getStringExtra("number")!!,
                        id
                    )
                binding.messageInput.editableText.clear()
            }
        }

        binding.openImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            activityForResult.launch(intent)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStop() {
        super.onStop()
        GlobalScope.launch(Dispatchers.IO) {
            if (time != null && lastMessage != null) {
                val number = intent.getStringExtra("number")!!
                firebaseDao.updateLastTime(id, number, time!!, lastMessage!!)
            }
        }
    }

}