package com.example.news_app.Fragments

import android.os.Bundle
import android.provider.Settings
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SetPasswordDialogFragment : DialogFragment() {
    private lateinit var v: View

    private lateinit var btn_ok: Button
    private lateinit var btn_cancel: Button
    private lateinit var et_password: EditText
    private lateinit var et_confirm_password: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.dialog_fragment_set_password, container, false)

        initDatabase()

        btn_ok = v.findViewById(R.id.btn_ok)
        btn_cancel = v.findViewById(R.id.btn_cancel)
        et_password = v.findViewById(R.id.et_password)
        et_confirm_password = v.findViewById(R.id.et_confirm_password)

        btn_ok.setOnClickListener {
            val password = et_password.text.toString()
            val confirm_password = et_confirm_password.text.toString()

            if (!password.isNullOrBlank() && (password == confirm_password)) {
                addPasswordToDatabase(password)
                SettingsFragment.switch_notes_security.isChecked = true
            } else {
                SettingsFragment.switch_notes_security.isChecked = false
                Toast.makeText(requireContext(), "Passwords must be same", Toast.LENGTH_SHORT)
                    .show()
            }
            dismiss()
        }

        btn_cancel.setOnClickListener {
            dismiss()
        }

        return v
    }

    private fun addPasswordToDatabase(password: String) {
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
}