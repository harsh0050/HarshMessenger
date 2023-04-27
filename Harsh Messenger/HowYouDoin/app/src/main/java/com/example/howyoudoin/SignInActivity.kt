package com.example.howyoudoin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.howyoudoin.databinding.ActivitySignInBinding
import com.example.howyoudoin.firebasedao.FirebaseDao
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {
    lateinit var binding : ActivitySignInBinding
    lateinit var mobileNumInput: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val thisContext = this
        if(Firebase.auth.currentUser!=null){
            startActivity(Intent(thisContext, HomePageActivity::class.java))
            finish()
        }
        val callback= object : OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(authCredential: PhoneAuthCredential) {
                signInWithCredentials(thisContext, authCredential, binding.progressBar, binding.loadingMsg)
            }
            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(thisContext, "Invalid Format", Toast.LENGTH_SHORT).show()
                startActivity(Intent(thisContext, SignInActivity::class.java))
            }

            override fun onCodeSent(verificationId: String, resendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, resendingToken)
                val intent = Intent(thisContext,EnterOtpActivity::class.java)
                intent.putExtra("verificationId", verificationId)
                intent.putExtra("mobileNumber", mobileNumInput)
                binding.loadingMsg.visibility= View.GONE
                binding.progressBar.visibility = View.GONE
                startActivity(intent)
            }

        }
        binding.sendCodeButton.setOnClickListener{
            binding.loadingMsg.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
            mobileNumInput = binding.phoneNumberInput.text.toString()
            val options = PhoneAuthOptions.newBuilder()
                .setActivity(this)
                .setPhoneNumber(mobileNumInput)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callback)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

    }
    companion object{
        fun signInWithCredentials(context: AppCompatActivity, authCredential: PhoneAuthCredential, one: ProgressBar, two: TextView) {
            Firebase.auth.signInWithCredential(authCredential).addOnCompleteListener(context){task->
                one.visibility = View.GONE
                two.visibility = View.GONE
                if(task.isSuccessful){
                    val firebaseDao = FirebaseDao(context)
                    firebaseDao.addUser(task.result.user!!.phoneNumber!!)
                    val homePageIntent = Intent(context, HomePageActivity::class.java)
                    context.startActivity(homePageIntent)
                    context.finish()
                }else{
                    val e = task.exception
                    if(e is FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

}