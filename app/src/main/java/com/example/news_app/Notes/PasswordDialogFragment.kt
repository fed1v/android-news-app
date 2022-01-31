package com.example.news_app.Notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.news_app.Helpers.DatabaseHelper
import com.example.news_app.Helpers.EncryptionHelper
import com.example.news_app.SettingsFragment
import com.example.news_app.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PasswordDialogFragment : DialogFragment() {
    private lateinit var v: View
    private lateinit var btn_confirm: Button
    private lateinit var btn_cancel: Button
    private lateinit var et_password: EditText

    private lateinit var databaseHelper: DatabaseHelper
    private var password: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.dialog_fragment_password, container, false)
        databaseHelper = DatabaseHelper(requireContext())
        getPasswordFromDatabase()
        initView()
        return v
    }

    private fun initView() {
        btn_confirm = v.findViewById(R.id.btn_confirm)
        btn_cancel = v.findViewById(R.id.btn_cancel)
        et_password = v.findViewById(R.id.et_password)

        btn_confirm.setOnClickListener {
            val pass = et_password.text.toString()
            val passHash = EncryptionHelper.getSHA256(pass)
            if (!password.isNullOrBlank() && (passHash == password)) {
                deletePasswordFromDatabase()
            } else {
                Toast.makeText(requireContext(), "Wrong password", Toast.LENGTH_SHORT).show()
                SettingsFragment.switch_notes_security.isChecked = true
            }
            dismiss()
        }
        btn_cancel.setOnClickListener {
            SettingsFragment.switch_notes_security.isChecked = true
            dismiss()
        }
    }

    private fun deletePasswordFromDatabase() {
        databaseHelper.currentUserReference.child("notesPassword").removeValue()
    }

    private fun getPasswordFromDatabase() {
        databaseHelper.currentUserReference.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                password = snapshot.child("notesPassword").getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}