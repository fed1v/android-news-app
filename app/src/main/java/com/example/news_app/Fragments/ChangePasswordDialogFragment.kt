package com.example.news_app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.news_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChangePasswordDialogFragment : DialogFragment() {
    private lateinit var v: View

    private lateinit var btn_ok: Button
    private lateinit var btn_cancel: Button

    private lateinit var et_old_password: EditText
    private lateinit var et_new_password: EditText
    private lateinit var et_confirm_password: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private lateinit var userNotesReference: DatabaseReference
    private var user: FirebaseUser? = null

    private var password: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.dialog_fragment_change_password, container, false)

        initDatabase()
        getPasswordFromDatabase()

        btn_ok = v.findViewById(R.id.btn_ok)
        btn_cancel = v.findViewById(R.id.btn_cancel)

        et_old_password = v.findViewById(R.id.et_old_password)
        et_new_password = v.findViewById(R.id.et_new_password)
        et_confirm_password = v.findViewById(R.id.et_confirm_password)

        btn_ok.setOnClickListener {
            val old_password = et_old_password.text.toString()
            val new_password = et_new_password.text.toString()
            val confirm_password = et_confirm_password.text.toString()

            if (!password.isNullOrBlank() && (password == old_password)) {
                if (!new_password.isNullOrBlank() && (new_password == confirm_password)) {
                    addNewPasswordToDatabase(new_password)
                    Toast.makeText(context, "Password changed", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(
                        context,
                        "New password and Confirm password must be same",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearFields()
                }
            } else {
                clearFields()
                Toast.makeText(requireContext(), "Old password is wrong", Toast.LENGTH_SHORT).show()
            }
        }

        btn_cancel.setOnClickListener {
            println("Cancel")
            dismiss()
        }

        return v
    }

    private fun clearFields() {
        et_old_password.text = null
        et_new_password.text = null
        et_confirm_password.text = null
    }

    private fun addNewPasswordToDatabase(password: String) {
        currentUserReference.updateChildren(mapOf("notesPassword" to password))
    }

    private fun initDatabase() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user!!.uid)
            userBookmarksReference = currentUserReference.child("bookmarks")
            userStatsReference = currentUserReference.child("stats")
        }
    }

    private fun getPasswordFromDatabase() {
        currentUserReference.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                password = snapshot.child("notesPassword").getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}