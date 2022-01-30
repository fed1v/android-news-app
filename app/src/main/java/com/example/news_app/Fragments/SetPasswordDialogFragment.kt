package com.example.news_app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.news_app.DatabaseHelper
import com.example.news_app.EncryptionHelper
import com.example.news_app.R

class SetPasswordDialogFragment : DialogFragment() {
    private lateinit var v: View

    private lateinit var btn_ok: Button
    private lateinit var btn_cancel: Button
    private lateinit var et_password: EditText
    private lateinit var et_confirm_password: EditText

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.dialog_fragment_set_password, container, false)

        databaseHelper = DatabaseHelper(requireContext())
        initView()

        return v
    }

    private fun initView() {
        btn_ok = v.findViewById(R.id.btn_ok)
        btn_cancel = v.findViewById(R.id.btn_cancel)
        et_password = v.findViewById(R.id.et_password)
        et_confirm_password = v.findViewById(R.id.et_confirm_password)

        btn_ok.setOnClickListener {
            val password = et_password.text.toString()
            val confirm_password = et_confirm_password.text.toString()

            if (!password.isNullOrBlank() && (password == confirm_password)) {
                val passwordHash = EncryptionHelper.getSHA256(password)
                addPasswordToDatabase(passwordHash)
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
    }

    private fun addPasswordToDatabase(password: String) {
        databaseHelper.currentUserReference.updateChildren(mapOf("notesPassword" to password))
    }
}