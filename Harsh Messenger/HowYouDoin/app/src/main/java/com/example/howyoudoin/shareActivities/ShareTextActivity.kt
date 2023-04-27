package com.example.howyoudoin.shareActivities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.howyoudoin.R
import com.example.howyoudoin.adapter.ShareTextAdapter
import com.example.howyoudoin.adapter.ShareTextListener
import com.example.howyoudoin.adapter.ShareTextViewHolder
import com.example.howyoudoin.databinding.ActivityShareTextBinding
import com.example.howyoudoin.localDatabase.ChatDao
import com.example.howyoudoin.localDatabase.ChatDatabase

class ShareTextActivity : AppCompatActivity(), ShareTextListener {
    lateinit var binding:ActivityShareTextBinding
//    private val firebaseDao = FirebaseDao()
    private lateinit var chatDao: ChatDao
    var currentSelected : ShareTextViewHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.shareTextToolbar)

        chatDao = ChatDatabase.getDatabase(this).getChatDao()

        binding.shareTextToRecyclerView.layoutManager = LinearLayoutManager(this)
        val shareTextAdapter = ShareTextAdapter(this)
        binding.shareTextToRecyclerView.adapter = shareTextAdapter
        chatDao.getAllChats().observe(this){
            shareTextAdapter.updateList(it)
        }
//        val query = firebaseDao.getChatQuery("lastTime", Query.Direction.DESCENDING)
//        val options = FirestoreRecyclerOptions.Builder<Contact>()
//            .setLifecycleOwner(this)
//            .setQuery(query, Contact::class.java)
//            .build()
//        val shareTextAdapter = ShareTextAdapter(this, options)


    }

    override fun onClick(holder: ShareTextViewHolder) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if(holder.isSelected){
            holder.itemView.setBackgroundColor(ContextCompat.getColor(this, com.firebase.ui.auth.R.color.fui_transparent))
            holder.selectedPhoto.visibility = View.GONE
            holder.isSelected = false
            currentSelected=null
            supportActionBar?.title = "Send to..."
            fragmentTransaction.remove(supportFragmentManager.findFragmentByTag("SEND_SHEET_TAG")!!).commit()

        }else{
            holder.itemView.setBackgroundColor(Color.parseColor("#E4E4E4"))
            holder.selectedPhoto.visibility = View.VISIBLE
            if(currentSelected!=null){
                currentSelected?.let{
                    it.itemView.setBackgroundColor(ContextCompat.getColor(this, com.firebase.ui.auth.R.color.fui_transparent))
                    it.selectedPhoto.visibility = View.GONE
                    it.isSelected = false
                }
            }
            currentSelected = holder
            fragmentTransaction.setReorderingAllowed(true)
            holder.isSelected = true
            supportActionBar?.title = holder.name.text.toString()
            val type = intent.type!!
            val bundle = bundleOf("sendToId" to holder.id, "sendToName" to holder.name.text.toString(), "sendToNumber" to holder.number)

            if(type == "text/plain"){
                bundle.putString("mimeType", "text/plain")
                bundle.putString("text", intent.getStringExtra(Intent.EXTRA_TEXT))
            }
            fragmentTransaction.replace(R.id.sendSheet, SendSheet::class.java, bundle,"SEND_SHEET_TAG")
            fragmentTransaction.commitNow()
        }
    }

}