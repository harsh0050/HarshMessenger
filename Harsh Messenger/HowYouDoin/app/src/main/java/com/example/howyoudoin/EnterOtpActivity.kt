package com.example.howyoudoin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.howyoudoin.databinding.ActivityEnterOtpBinding
import com.google.firebase.auth.PhoneAuthProvider

class EnterOtpActivity : AppCompatActivity() {
    lateinit var binding : ActivityEnterOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.phoneNumber.text = intent.getStringExtra("mobileNumber").toString()
        binding.signInButton.setOnClickListener{
            binding.loadingMsg.visibility= View.VISIBLE
            binding.progressBar.visibility=View.VISIBLE
            val credential = PhoneAuthProvider
                .getCredential(intent.getStringExtra("verificationId")!!, binding.otpInput.text.toString())
            SignInActivity.signInWithCredentials(this, credential, binding.progressBar, binding.loadingMsg)
        }

    }
}