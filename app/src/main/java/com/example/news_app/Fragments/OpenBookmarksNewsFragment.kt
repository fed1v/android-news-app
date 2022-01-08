package com.example.news_app.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.R
import com.google.common.hash.Hashing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.nio.charset.Charset

class OpenBookmarksNewsFragment(var headlines: NewsHeadlines) : Fragment() {
    private lateinit var v: View
    private lateinit var toolbar: Toolbar
    private lateinit var webView: WebView

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private var user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(context).inflate(R.layout.fragment_open_bookmarks_news, container, false)

        webView = v.findViewById(R.id.web_view_open_bookmarks_news)
        webView.webViewClient = WebViewClient()
        webView.loadUrl(headlines.url)

        toolbar = v.findViewById(R.id.toolbar_open_bookmarks_news)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.mi_share -> shareLink()
                R.id.mi_delete -> deleteFromBookmarks()
                R.id.mi_notes -> openNotes()
            }
            true
        }

        initDatabase()

        requireActivity().onBackPressedDispatcher.addCallback{
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, BookmarksFragment())
                .commit()
        }

        return v
    }

    private fun initDatabase() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user!!.uid)
            userBookmarksReference = currentUserReference.child("bookmarks")
        }
    }

    private fun openNotes() {
        //TODO
        println("Open Notes")
    }

    private fun deleteFromBookmarks() {
        val urlHashCode =
            Hashing.sha1().hashString(headlines.url, Charset.defaultCharset()).toString()
        userBookmarksReference.child(urlHashCode).removeValue()
        Toast.makeText(requireContext(), "Bookmark deleted", Toast.LENGTH_SHORT).show()
    }

    private fun shareLink() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, headlines.url)
            type = "text/plain"
        }
        startActivity(shareIntent)
    }

}