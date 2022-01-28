package com.example.news_app.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.Models.Note
import com.example.news_app.NotesAdapter
import com.example.news_app.R
import com.example.news_app.SelectInNotesListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.nio.charset.Charset

class NotesFragment(var headlines: NewsHeadlines) : Fragment(), SelectInNotesListener {
    private lateinit var v: View
    private lateinit var btn_add: FloatingActionButton
    private lateinit var dialog: AddNoteDialogFragment

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotesAdapter
    private lateinit var viewHolder: NotesAdapter.NotesViewHolder

    private lateinit var notes: ArrayList<Note>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_notes, container, false)

        recyclerView = v.findViewById(R.id.recycler_view_notes)

        initDatabase()

        getNotes()

        dialog = AddNoteDialogFragment(headlines)

        btn_add = v.findViewById(R.id.btn_add_note)
        btn_add.setOnClickListener {
            try{
                dialog.show(requireActivity().supportFragmentManager, "Add note")
            } catch(e: Exception){
                e.printStackTrace()
            }
        }

        activity?.onBackPressedDispatcher?.addCallback{
            activity?.supportFragmentManager?.popBackStack()
        }

        return v
    }

    private fun getNotes() {
        notes = arrayListOf()
        val bookmarkHashCode = Hashing.sha1().hashString(headlines.url, Charset.defaultCharset()).toString()
        userBookmarksReference.child(bookmarkHashCode).child("notes").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                notes.clear()
                for(dataSnapshot in snapshot.children){
                    val note = dataSnapshot.getValue(Note::class.java)
                    if(note != null){
                        notes.add(note)
                    }
                }
                showNotes(notes)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showNotes(notes: java.util.ArrayList<Note>) {
        if(context != null){
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = NotesAdapter(requireContext(), notes, this)
            recyclerView.adapter = adapter
        }
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

    override fun onNoteClicked(note: Note) {
        val dialog = OpenNoteDialogFragment(note, headlines)
        try {
            dialog.show(requireActivity().supportFragmentManager, "Open note")
        } catch(e: Exception){
            e.printStackTrace()
        }
    }
}