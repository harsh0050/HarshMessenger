package com.example.howyoudoin.Utility

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.howyoudoin.firebasedao.FirebaseDao

class SelectContactDialogueBox(val number: String, val name: String, val context: AppCompatActivity) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val firebaseDao = FirebaseDao(context)
        val dialogBoxBuilder = AlertDialog.Builder(activity)
            .setMessage("Add this chat?")
            .setPositiveButton("Yes") { _, _ ->
                firebaseDao.addNewChat(number, name, System.currentTimeMillis())
                context.finish()
            }.setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        return dialogBoxBuilder.create()

    }
}