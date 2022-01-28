package com.example.news_app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.Note
import com.example.news_app.R
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.nio.charset.Charset

class AddNoteDialogFragment(var headlines: NewsHeadlines): DialogFragment() {
    private lateinit var v: View
    private lateinit var btn_ok: Button
    private lateinit var btn_cancel: Button
    private lateinit var et_title: EditText
    private lateinit var et_description: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private lateinit var userNotesReference: DatabaseReference
    private var user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.dialog_fragment_add_note, container, false)

        initDatabase()

        btn_ok = v.findViewById(R.id.btn_ok)
        btn_cancel = v.findViewById(R.id.btn_cancel)

        et_title = v.findViewById(R.id.et_title)
        et_description = v.findViewById(R.id.et_description)

        btn_ok.setOnClickListener {
            val title = et_title.text.toString()
            val description = et_description.text.toString()
            val createdTime = System.currentTimeMillis()
            println(title)
            println(description)
            addNoteToDatabase(title, description, createdTime)
            dismiss()
            clearEditTexts()
        }

        btn_cancel.setOnClickListener {
            dismiss()
            clearEditTexts()
        }


        return v
    }

    private fun addNoteToDatabase(title: String, description: String, createdTime: Long){
        val id = Long.MAX_VALUE - createdTime
        userNotesReference.child(id.toString()).setValue(Note(title, description, createdTime))

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
            val urlHashCode =
                Hashing.sha1().hashString(headlines.url, Charset.defaultCharset()).toString()
            userNotesReference = userBookmarksReference.child(urlHashCode).child("notes")
        }
    }

    private fun clearEditTexts() {
        et_title.text = null
        et_description.text = null
    }
}