package com.example.howyoudoin

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.howyoudoin.Utility.SelectContactDialogueBox
import com.example.howyoudoin.adapter.ContactViewHolder
import com.example.howyoudoin.adapter.CustomClickListener
import com.example.howyoudoin.adapter.SelectContactsAdapter
import com.example.howyoudoin.databinding.ActivitySelectContactBinding
import com.example.howyoudoin.model.Contact

class SelectContactActivity : AppCompatActivity(), CustomClickListener {
    private lateinit var binding: ActivitySelectContactBinding
    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.selectContactToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Select contact"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val selectContactAdapter = SelectContactsAdapter(this)
        binding.recyclerView.adapter = selectContactAdapter
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted->run{
                if(isGranted){
                    selectContactAdapter.update(getContactList())
                }else{
                    Toast.makeText(this, "Can't Display Contacts", Toast.LENGTH_SHORT).show()
                }
            }
        }
        requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
    }


    @SuppressLint("Range")
    fun getContactList() :ArrayList<Contact>{
        binding.selectContactsProgressBar.visibility = View.VISIBLE

        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME+" ASC")
        val contacts = ArrayList<Contact>()
        if(cursor!=null){
            while(cursor.moveToNext()){
                val hasPhoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt()
                if(hasPhoneNumber>0){//true
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    var number = ""

                    val numberFindCursor = contentResolver
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null)
                    if(numberFindCursor!=null){
                        if(numberFindCursor.moveToNext()){
                            number = numberFindCursor
                                .getString(numberFindCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                        numberFindCursor.close()
                    }
                    val tempContact = Contact(name,number,"just kidding")
                    contacts.add(tempContact)
                }
            }
            cursor.close()
        }
        binding.selectContactsProgressBar.visibility=View.GONE
        return contacts
    }

    override fun onClick(holder: ContactViewHolder) {
        val dialogBox = SelectContactDialogueBox(holder.number.text.toString(),holder.name.text.toString(), this)
        dialogBox.show(this.supportFragmentManager, "Select Contact")
    }
}
