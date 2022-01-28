package com.example.news_app

import com.example.news_app.Models.Note

interface SelectInNotesListener {
    fun onNoteClicked(note: Note)
}