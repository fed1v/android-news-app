package com.example.news_app.Helpers

import android.content.Context
import android.widget.Toast
import com.example.news_app.Models.NewsHeadlines
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DatabaseHelper() {
    private lateinit var context: Context

    lateinit var auth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var usersReference: DatabaseReference
    lateinit var currentUserReference: DatabaseReference
    lateinit var userBookmarksReference: DatabaseReference
    lateinit var userStatsReference: DatabaseReference
    lateinit var userNotesReference: DatabaseReference
    var user: FirebaseUser? = null

    constructor(context: Context, news: NewsHeadlines? = null) : this() {
        this.context = context
        initDatabase(news)
    }

    private fun initDatabase(news: NewsHeadlines? = null) {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user!!.uid)
            userBookmarksReference = currentUserReference.child("bookmarks")
            userStatsReference = currentUserReference.child("stats")
            if(news != null){
                val urlHashCode = EncryptionHelper.getSHA1(news.url)
                userNotesReference = userBookmarksReference.child(urlHashCode).child("notes")
            }
        }
    }

    fun addToBookmarks(news: NewsHeadlines, category: String? = null) {
        if (news.category == null) {
            news.category = category
        }
        val urlHashCode = EncryptionHelper.getSHA1(news.url)
        userBookmarksReference.child(urlHashCode).setValue(news)
        Toast.makeText(context, "Bookmark added", Toast.LENGTH_SHORT).show()
    }

    fun deleteFromBookmarks(news: NewsHeadlines) {
        val urlHashCode = EncryptionHelper.getSHA1(news.url)
        userBookmarksReference.child(urlHashCode).removeValue()
        Toast.makeText(context, "Bookmark deleted", Toast.LENGTH_SHORT).show()
    }

}