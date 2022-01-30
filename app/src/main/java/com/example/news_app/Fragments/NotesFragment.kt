package com.example.news_app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.*
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class NotesFragment(var headlines: NewsHeadlines) : Fragment(), SelectInNotesListener {
    private lateinit var v: View
    private lateinit var btn_add: FloatingActionButton
    private lateinit var dialog: AddNoteDialogFragment

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotesAdapter

    private lateinit var notes: ArrayList<Note>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_notes, container, false)

        if (!InternetConnection.isConnected()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            return v
        }

        databaseHelper = DatabaseHelper(requireContext())
        getNotes()
        initView()

        return v
    }

    private fun initView() {
        recyclerView = v.findViewById(R.id.recycler_view_notes)

        dialog = AddNoteDialogFragment(headlines)

        btn_add = v.findViewById(R.id.btn_add_note)
        btn_add.setOnClickListener {
            try {
                dialog.show(requireActivity().supportFragmentManager, "Add note")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        activity?.onBackPressedDispatcher?.addCallback {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun getNotes() {
        notes = arrayListOf()
        val bookmarkHashCode = EncryptionHelper.getSHA1(headlines.url)
        databaseHelper.userBookmarksReference.child(bookmarkHashCode).child("notes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notes.clear()
                    for (dataSnapshot in snapshot.children) {
                        val note = dataSnapshot.getValue(Note::class.java)
                        if (note != null) {
                            notes.add(note)
                        }
                    }
                    showNotes(notes)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showNotes(notes: java.util.ArrayList<Note>) {
        if (context != null) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = NotesAdapter(requireContext(), notes, this)
            recyclerView.adapter = adapter
        }
    }

    override fun onNoteClicked(note: Note) {
        val dialog = OpenNoteDialogFragment(note, headlines)
        try {
            dialog.show(requireActivity().supportFragmentManager, "Open note")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}