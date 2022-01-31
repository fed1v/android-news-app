package com.example.news_app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.example.news_app.DatabaseHelper
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.Note
import com.example.news_app.R

class OpenNoteDialogFragment(var note: Note, var headlines: NewsHeadlines) : DialogFragment() {
    private lateinit var v: View
    private lateinit var btn_delete: ImageButton
    private lateinit var btn_save: Button
    private lateinit var btn_cancel: Button
    private lateinit var et_title: EditText
    private lateinit var et_description: EditText

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.dialog_fragment_open_note, container, false)
        databaseHelper = DatabaseHelper(requireContext(), headlines)
        initView()
        return v
    }

    private fun initView() {
        btn_delete = v.findViewById(R.id.btn_delete)
        btn_save = v.findViewById(R.id.btn_save)
        btn_cancel = v.findViewById(R.id.btn_cancel)
        et_title = v.findViewById(R.id.et_title)
        et_description = v.findViewById(R.id.et_description)

        et_title.setText(note.title)
        et_description.setText(note.description)

        btn_delete.setOnClickListener {
            deleteNote(note.createdTime)
            dismiss()
        }
        btn_save.setOnClickListener {
            val title = et_title.text.toString()
            val description = et_description.text.toString()
            saveNote(title, description, note.createdTime)
            dismiss()
        }
        btn_cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveNote(title: String?, description: String, createdTime: Long) {
        val id = Long.MAX_VALUE - createdTime
        databaseHelper.userNotesReference.child(id.toString())
            .setValue(Note(title, description, createdTime))
    }

    private fun deleteNote(createdTime: Long) {
        val id = Long.MAX_VALUE - createdTime
        databaseHelper.userNotesReference.child(id.toString()).removeValue()
    }
}