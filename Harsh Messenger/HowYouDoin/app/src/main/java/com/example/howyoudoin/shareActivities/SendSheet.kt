package com.example.howyoudoin.shareActivities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.howyoudoin.ChatActivity
import com.example.howyoudoin.HomePageActivity
import com.example.howyoudoin.SignInActivity
import com.example.howyoudoin.databinding.FragmentSendSheetBinding
import com.example.howyoudoin.firebasedao.FirebaseDao

class SendSheet : Fragment() {
    private var sendToName: String? = null
    private var sendToNumber: String? = null
    private var sendToId: String? = null
    private var message: String? = null
    private var mimeType: String? = null
    private lateinit var intentLauncher: ActivityResultLauncher<Intent>
    private lateinit var firebaseDao: FirebaseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                activity?.startActivity(Intent(activity, SignInActivity::class.java))
            }
        firebaseDao = FirebaseDao((activity?.applicationContext!!))
        arguments?.let {
            sendToName = it.getString("sendToName")
            sendToNumber = it.getString("sendToNumber")
            sendToId = it.getString("sendToId")
            message = it.getString("text")
            mimeType = it.getString("mimeType")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val layout = FragmentSendSheetBinding.inflate(layoutInflater)
        layout.sentToTextView.text = sendToName
        layout.sendToButton.setOnClickListener {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra("id", sendToId)
            intent.putExtra("name", sendToName)
            intent.putExtra("number", sendToNumber)
            if (mimeType == "text/plain") {
                intent.putExtra("text", message)
                startActivity(Intent(activity, HomePageActivity::class.java))
            }
            activity?.finish()
            startActivity(intent)

        }
        return layout.root
    }
}